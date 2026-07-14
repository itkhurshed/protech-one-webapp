package com.protechone.controller;

import com.protechone.dto.purchasing.PurchaseInvoiceItemRequest;
import com.protechone.dto.purchasing.PurchaseInvoiceRequest;
import com.protechone.dto.sales.PaymentRequest;
import com.protechone.exception.BadRequestException;
import com.protechone.service.InventoryService;
import com.protechone.service.PurchaseInvoiceService;
import com.protechone.service.SupplierService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PurchaseInvoiceController {

    private static final int LINE_ITEM_ROWS = 10;

    private final PurchaseInvoiceService purchaseInvoiceService;
    private final SupplierService supplierService;
    private final InventoryService inventoryService;

    @GetMapping("/purchasing/invoices")
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        var result = purchaseInvoiceService.list(PageRequest.of(page, 20));
        model.addAttribute("invoices", result.getContent());
        model.addAttribute("pageObj", result);
        return "purchasing/invoices-list";
    }

    @GetMapping("/purchasing/invoices/new")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER','BRANCH_MANAGER')")
    public String newForm(Model model) {
        PurchaseInvoiceForm form = new PurchaseInvoiceForm();
        form.setInvoiceDate(LocalDate.now());
        for (int i = 0; i < LINE_ITEM_ROWS; i++) form.getItems().add(new ItemForm());
        model.addAttribute("form", form);
        model.addAttribute("suppliers", supplierService.list(null, PageRequest.of(0, 500)).getContent());
        model.addAttribute("warehouses", inventoryService.listWarehouses());
        model.addAttribute("products", inventoryService.listProducts(null, PageRequest.of(0, 500)).getContent());
        return "purchasing/invoice-form";
    }

    @PostMapping("/purchasing/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER','BRANCH_MANAGER')")
    public String create(@ModelAttribute PurchaseInvoiceForm form, RedirectAttributes redirectAttributes) {
        List<PurchaseInvoiceItemRequest> items = form.getItems().stream()
                .filter(i -> i.getProductId() != null)
                .map(i -> new PurchaseInvoiceItemRequest(i.getProductId(),
                        nz(i.getQuantity()), nz(i.getUnitCost()), nz(i.getDiscountPct()), nz(i.getTaxRate())))
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Add at least one line item with a product selected.");
            return "redirect:/purchasing/invoices/new";
        }
        try {
            var invoice = purchaseInvoiceService.create(new PurchaseInvoiceRequest(form.getSupplierId(), form.getWarehouseId(),
                    form.getInvoiceDate(), form.getDueDate(), "DRAFT", form.getNotes(), items));
            redirectAttributes.addFlashAttribute("successMessage", "Purchase invoice " + invoice.invoiceNumber() + " created as draft.");
            return "redirect:/purchasing/invoices/" + invoice.id();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/purchasing/invoices/new";
        }
    }

    @GetMapping("/purchasing/invoices/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("invoice", purchaseInvoiceService.get(id));
        return "purchasing/invoice-view";
    }

    @PostMapping("/purchasing/invoices/{id}/confirm")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER','BRANCH_MANAGER')")
    public String confirm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            purchaseInvoiceService.confirm(id);
            redirectAttributes.addFlashAttribute("successMessage", "Invoice confirmed and stock received.");
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/purchasing/invoices/" + id;
    }

    @PostMapping("/purchasing/invoices/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER')")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        purchaseInvoiceService.cancel(id);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice cancelled.");
        return "redirect:/purchasing/invoices/" + id;
    }

    @PostMapping("/purchasing/invoices/{id}/pay")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER','ACCOUNTANT')")
    public String pay(@PathVariable Long id, @RequestParam BigDecimal amount, @RequestParam(required = false) String method,
                       @RequestParam(required = false) String notes, RedirectAttributes redirectAttributes) {
        try {
            purchaseInvoiceService.recordPayment(id, new PaymentRequest(amount, method, notes));
            redirectAttributes.addFlashAttribute("successMessage", "Payment recorded.");
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/purchasing/invoices/" + id;
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    @Data
    public static class PurchaseInvoiceForm {
        private Long supplierId, warehouseId;
        private LocalDate invoiceDate;
        private LocalDate dueDate;
        private String notes;
        private List<ItemForm> items = new ArrayList<>();
    }

    @Data
    public static class ItemForm {
        private Long productId;
        private BigDecimal quantity;
        private BigDecimal unitCost;
        private BigDecimal discountPct = BigDecimal.ZERO;
        private BigDecimal taxRate = BigDecimal.ZERO;
    }
}
