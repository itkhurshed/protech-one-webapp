package com.protechone.service;

import com.protechone.dto.sales.*;
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
 * Sales workflow: Quotation -> Sales Order -> Delivery -> Invoice is
 * simplified in this Phase 1 core into a single Sales Invoice document
 * (status DRAFT/CONFIRMED/PAID/...). Confirming an invoice deducts stock
 * from the chosen warehouse via InventoryService, keeping inventory and
 * accounts receivable in sync.
 */
@Service
@RequiredArgsConstructor
public class SalesInvoiceService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryService inventoryService;
    private final CurrentUser currentUser;

    public Page<SalesInvoiceResponse> list(Pageable pageable) {
        return salesInvoiceRepository.findByCompanyIdOrderByInvoiceDateDesc(currentUser.companyId(), pageable)
                .map(this::toResponse);
    }

    public SalesInvoiceResponse get(Long id) {
        return toResponse(findOwned(id));
    }

    @Transactional
    public SalesInvoiceResponse create(SalesInvoiceRequest request) {
        Company company = currentUser.get().getCompany();
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Warehouse warehouse = request.warehouseId() != null
                ? warehouseRepository.findById(request.warehouseId()).orElse(null) : null;

        SalesInvoice invoice = SalesInvoice.builder()
                .company(company)
                .customer(customer)
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

        for (SalesInvoiceItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));

            BigDecimal lineGross = itemReq.unitPrice().multiply(itemReq.quantity());
            BigDecimal discountPct = itemReq.discountPct() == null ? BigDecimal.ZERO : itemReq.discountPct();
            BigDecimal taxRate = itemReq.taxRate() == null ? BigDecimal.ZERO : itemReq.taxRate();
            BigDecimal discountAmt = lineGross.multiply(discountPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal netBeforeTax = lineGross.subtract(discountAmt);
            BigDecimal taxAmt = netBeforeTax.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = netBeforeTax.add(taxAmt);

            invoice.getItems().add(SalesInvoiceItem.builder()
                    .salesInvoice(invoice)
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(itemReq.unitPrice())
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

        SalesInvoice saved = salesInvoiceRepository.save(invoice);

        if (!"DRAFT".equalsIgnoreCase(saved.getStatus()) && warehouse != null) {
            deductStockForInvoice(saved, warehouse);
        }

        return toResponse(saved);
    }

    @Transactional
    public SalesInvoiceResponse confirm(Long id) {
        SalesInvoice invoice = findOwned(id);
        if (!"DRAFT".equalsIgnoreCase(invoice.getStatus())) {
            throw new BadRequestException("Only draft invoices can be confirmed");
        }
        if (invoice.getWarehouse() == null) {
            throw new BadRequestException("A warehouse must be set before confirming a sales invoice");
        }
        invoice.setStatus("CONFIRMED");
        deductStockForInvoice(invoice, invoice.getWarehouse());
        return toResponse(salesInvoiceRepository.save(invoice));
    }

    @Transactional
    public SalesInvoiceResponse recordPayment(Long id, PaymentRequest request) {
        SalesInvoice invoice = findOwned(id);
        BigDecimal newPaid = invoice.getAmountPaid().add(request.amount());
        if (newPaid.compareTo(invoice.getGrandTotal()) > 0) {
            throw new BadRequestException("Payment exceeds the remaining balance due");
        }
        invoice.setAmountPaid(newPaid);
        invoice.setStatus(newPaid.compareTo(invoice.getGrandTotal()) == 0 ? "PAID" : "PARTIALLY_PAID");
        return toResponse(salesInvoiceRepository.save(invoice));
    }

    @Transactional
    public void cancel(Long id) {
        SalesInvoice invoice = findOwned(id);
        invoice.setStatus("CANCELLED");
        salesInvoiceRepository.save(invoice);
    }

    private void deductStockForInvoice(SalesInvoice invoice, Warehouse warehouse) {
        for (SalesInvoiceItem item : invoice.getItems()) {
            inventoryService.applyMovement(item.getProduct(), warehouse, item.getQuantity().negate(),
                    "OUT", "SALES_INVOICE", invoice.getId(), "Sale " + invoice.getInvoiceNumber());
        }
    }

    private String nextInvoiceNumber(Long companyId) {
        long count = salesInvoiceRepository.countByCompanyId(companyId) + 1;
        return "INV-" + LocalDate.now().getYear() + "-" + String.format("%05d", count);
    }

    private SalesInvoice findOwned(Long id) {
        SalesInvoice invoice = salesInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales invoice not found: " + id));
        if (!invoice.getCompany().getId().equals(currentUser.companyId())) {
            throw new ResourceNotFoundException("Sales invoice not found: " + id);
        }
        return invoice;
    }

    private SalesInvoiceResponse toResponse(SalesInvoice inv) {
        List<SalesInvoiceItemResponse> items = inv.getItems().stream()
                .map(i -> new SalesInvoiceItemResponse(i.getId(), i.getProduct().getId(), i.getProduct().getName(),
                        i.getProduct().getSku(), i.getQuantity(), i.getUnitPrice(), i.getDiscountPct(), i.getTaxRate(), i.getLineTotal()))
                .toList();
        return new SalesInvoiceResponse(inv.getId(), inv.getInvoiceNumber(), inv.getCustomer().getId(),
                inv.getCustomer().getName(), inv.getInvoiceDate(), inv.getDueDate(), inv.getStatus(),
                inv.getSubtotal(), inv.getDiscountTotal(), inv.getTaxTotal(), inv.getGrandTotal(), inv.getAmountPaid(),
                inv.getGrandTotal().subtract(inv.getAmountPaid()), inv.getNotes(), inv.getCreatedAt(), items);
    }
}
