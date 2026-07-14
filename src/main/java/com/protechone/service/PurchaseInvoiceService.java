package com.protechone.service;

import com.protechone.dto.purchasing.*;
import com.protechone.entity.*;
import com.protechone.exception.BadRequestException;
import com.protechone.exception.ResourceNotFoundException;
import com.protechone.repository.*;
import com.protechone.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Purchasing workflow simplified to Purchase Invoices tied to a supplier.
 * Confirming an invoice increases stock in the chosen warehouse and
 * recalculates the product's cost price (simple moving weighted average
 * would be a Phase 2 refinement; Phase 1 uses latest cost for clarity).
 */
@Service
@RequiredArgsConstructor
public class PurchaseInvoiceService {

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryService inventoryService;
    private final CurrentUser currentUser;

    public Page<PurchaseInvoiceResponse> list(Pageable pageable) {
        return purchaseInvoiceRepository.findByCompanyIdOrderByInvoiceDateDesc(currentUser.companyId(), pageable)
                .map(this::toResponse);
    }

    public PurchaseInvoiceResponse get(Long id) {
        return toResponse(findOwned(id));
    }

    @Transactional
    public PurchaseInvoiceResponse create(PurchaseInvoiceRequest request) {
        Company company = currentUser.get().getCompany();
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        Warehouse warehouse = request.warehouseId() != null
                ? warehouseRepository.findById(request.warehouseId()).orElse(null) : null;

        PurchaseInvoice invoice = PurchaseInvoice.builder()
                .company(company)
                .supplier(supplier)
                .warehouse(warehouse)
                .invoiceNumber(nextInvoiceNumber(company.getId()))
                .invoiceDate(request.invoiceDate() != null ? request.invoiceDate() : LocalDate.now())
                .dueDate(request.dueDate())
                .status(request.status() != null ? request.status() : "DRAFT")
                .notes(request.notes())
                .amountPaid(BigDecimal.ZERO)
                .createdBy(currentUser.get())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO, discountTotal = BigDecimal.ZERO, taxTotal = BigDecimal.ZERO;

        for (PurchaseInvoiceItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));

            BigDecimal lineGross = itemReq.unitCost().multiply(itemReq.quantity());
            BigDecimal discountPct = itemReq.discountPct() == null ? BigDecimal.ZERO : itemReq.discountPct();
            BigDecimal taxRate = itemReq.taxRate() == null ? BigDecimal.ZERO : itemReq.taxRate();
            BigDecimal discountAmt = lineGross.multiply(discountPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal netBeforeTax = lineGross.subtract(discountAmt);
            BigDecimal taxAmt = netBeforeTax.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = netBeforeTax.add(taxAmt);

            invoice.getItems().add(PurchaseInvoiceItem.builder()
                    .purchaseInvoice(invoice)
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitCost(itemReq.unitCost())
                    .discountPct(discountPct)
                    .taxRate(taxRate)
                    .lineTotal(lineTotal)
                    .build());

            subtotal = subtotal.add(lineGross);
            discountTotal = discountTotal.add(discountAmt);
            taxTotal = taxTotal.add(taxAmt);
        }

        invoice.setSubtotal(subtotal);
        invoice.setDiscountTotal(discountTotal);
        invoice.setTaxTotal(taxTotal);
        invoice.setGrandTotal(subtotal.subtract(discountTotal).add(taxTotal));

        PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);

        if (!"DRAFT".equalsIgnoreCase(saved.getStatus()) && warehouse != null) {
            receiveStockForInvoice(saved, warehouse);
        }

        return toResponse(saved);
    }

    @Transactional
    public PurchaseInvoiceResponse confirm(Long id) {
        PurchaseInvoice invoice = findOwned(id);
        if (!"DRAFT".equalsIgnoreCase(invoice.getStatus())) {
            throw new BadRequestException("Only draft invoices can be confirmed");
        }
        if (invoice.getWarehouse() == null) {
            throw new BadRequestException("A warehouse must be set before confirming a purchase invoice");
        }
        invoice.setStatus("CONFIRMED");
        receiveStockForInvoice(invoice, invoice.getWarehouse());
        return toResponse(purchaseInvoiceRepository.save(invoice));
    }

    @Transactional
    public PurchaseInvoiceResponse recordPayment(Long id, com.protechone.dto.sales.PaymentRequest request) {
        PurchaseInvoice invoice = findOwned(id);
        BigDecimal newPaid = invoice.getAmountPaid().add(request.amount());
        if (newPaid.compareTo(invoice.getGrandTotal()) > 0) {
            throw new BadRequestException("Payment exceeds the remaining balance due");
        }
        invoice.setAmountPaid(newPaid);
        invoice.setStatus(newPaid.compareTo(invoice.getGrandTotal()) == 0 ? "PAID" : "PARTIALLY_PAID");
        return toResponse(purchaseInvoiceRepository.save(invoice));
    }

    @Transactional
    public void cancel(Long id) {
        PurchaseInvoice invoice = findOwned(id);
        invoice.setStatus("CANCELLED");
        purchaseInvoiceRepository.save(invoice);
    }

    private void receiveStockForInvoice(PurchaseInvoice invoice, Warehouse warehouse) {
        for (PurchaseInvoiceItem item : invoice.getItems()) {
            inventoryService.applyMovement(item.getProduct(), warehouse, item.getQuantity(),
                    "IN", "PURCHASE_INVOICE", invoice.getId(), "Purchase " + invoice.getInvoiceNumber());
            item.getProduct().setCostPrice(item.getUnitCost());
            productRepository.save(item.getProduct());
        }
    }

    private String nextInvoiceNumber(Long companyId) {
        long count = purchaseInvoiceRepository.countByCompanyId(companyId) + 1;
        return "PINV-" + LocalDate.now().getYear() + "-" + String.format("%05d", count);
    }

    private PurchaseInvoice findOwned(Long id) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase invoice not found: " + id));
        if (!invoice.getCompany().getId().equals(currentUser.companyId())) {
            throw new ResourceNotFoundException("Purchase invoice not found: " + id);
        }
        return invoice;
    }

    private PurchaseInvoiceResponse toResponse(PurchaseInvoice inv) {
        List<PurchaseInvoiceItemResponse> items = inv.getItems().stream()
                .map(i -> new PurchaseInvoiceItemResponse(i.getId(), i.getProduct().getId(), i.getProduct().getName(),
                        i.getProduct().getSku(), i.getQuantity(), i.getUnitCost(), i.getDiscountPct(), i.getTaxRate(), i.getLineTotal()))
                .toList();
        return new PurchaseInvoiceResponse(inv.getId(), inv.getInvoiceNumber(), inv.getSupplier().getId(),
                inv.getSupplier().getName(), inv.getInvoiceDate(), inv.getDueDate(), inv.getStatus(),
                inv.getSubtotal(), inv.getDiscountTotal(), inv.getTaxTotal(), inv.getGrandTotal(), inv.getAmountPaid(),
                inv.getGrandTotal().subtract(inv.getAmountPaid()), inv.getNotes(), inv.getCreatedAt(), items);
    }
}
