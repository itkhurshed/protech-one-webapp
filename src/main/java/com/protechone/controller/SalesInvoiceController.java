package com.protechone.controller;

import com.protechone.dto.sales.PaymentRequest;
import com.protechone.dto.sales.SalesInvoiceItemRequest;
import com.protechone.dto.sales.SalesInvoiceRequest;
import com.protechone.exception.BadRequestException;
import com.protechone.service.CustomerService;
import com.protechone.service.InventoryService;
import com.protechone.service.SalesInvoiceService;
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

/**
 * Sales invoices, no-JS edition. Line items are a fixed-size list of empty
 * rows rendered on the "new invoice" form (see LINE_ITEM_ROWS); Spring's
 * indexed data binding (items[0].productId, items[1].productId, ...) fills
 * whichever rows the user actually used, and rows left blank are dropped
 * before calling the service.
 */
@Controller
@RequiredArgsConstructor
public class SalesInvoiceController {

    private static final int LINE_ITEM_ROWS = 10;

    private final SalesInvoiceService salesInvoiceService;
    private final CustomerService customerService;
    private final InventoryService inventoryService;

    @GetMapping("/sales/invoices")
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        var result = salesInvoiceService.list(PageRequest.of(page, 20));
        model.addAttribute("invoices", result.getContent());
        model.addAttribute("pageObj", result);
        return "sales/invoices-list";
    }

    @GetMapping("/sales/invoices/new")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','SALES_MANAGER','CASHIER','BRANCH_MANAGER')")
    public String newForm(Model model) {
        SalesInvoiceForm form = new SalesInvoiceForm();
        form.setInvoiceDate(LocalDate.now());
        for (int i = 0; i < LINE_ITEM_ROWS; i++) form.getItems().add(new ItemForm());
        model.addAttribute("form", form);
        model.addAttribute("customers", customerService.list(null, PageRequest.of(0, 500)).getContent());
        model.addAttribute("warehouses", inventoryService.listWarehouses());
        model.addAttribute("products", inventoryService.listProducts(null, PageRequest.of(0, 500)).getContent());
        return "sales/invoice-form";
    }

    @PostMapping("/sales/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','SALES_MANAGER','CASHIER','BRANCH_MANAGER')")
    public String create(@ModelAttribute SalesInvoiceForm form, RedirectAttributes redirectAttributes) {
        List<SalesInvoiceItemRequest> items = form.getItems().stream()
                .filter(i -> i.getProductId() != null)
                .map(i -> new SalesInvoiceItemRequest(i.getProductId(),
                        nz(i.getQuantity()), nz(i.getUnitPrice()), nz(i.getDiscountPct()), nz(i.getTaxRate())))
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Add at least one line item with a product selected.");
            return "redirect:/sales/invoices/new";
        }
        try {
            var invoice = salesInvoiceService.create(new SalesInvoiceRequest(form.getCustomerId(), form.getWarehouseId(),
                    form.getInvoiceDate(), form.getDueDate(), "DRAFT", form.getNotes(), items));
            redirectAttributes.addFlashAttribute("successMessage", "Sales invoice " + invoice.invoiceNumber() + " created as draft.");
            return "redirect:/sales/invoices/" + invoice.id();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/sales/invoices/new";
        }
    }

    @GetMapping("/sales/invoices/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("invoice", salesInvoiceService.get(id));
        return "sales/invoice-view";
    }

    @PostMapping("/sales/invoices/{id}/confirm")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','SALES_MANAGER','BRANCH_MANAGER')")
    public String confirm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            salesInvoiceService.confirm(id);
            redirectAttributes.addFlashAttribute("successMessage", "Invoice confirmed and stock deducted.");
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/sales/invoices/" + id;
    }

    @PostMapping("/sales/invoices/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','SALES_MANAGER')")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        salesInvoiceService.cancel(id);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice cancelled.");
        return "redirect:/sales/invoices/" + id;
    }

    @PostMapping("/sales/invoices/{id}/pay")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','SALES_MANAGER','CASHIER','ACCOUNTANT')")
    public String pay(@PathVariable Long id, @RequestParam BigDecimal amount, @RequestParam(required = false) String method,
                       @RequestParam(required = false) String notes, RedirectAttributes redirectAttributes) {
        try {
            salesInvoiceService.recordPayment(id, new PaymentRequest(amount, method, notes));
            redirectAttributes.addFlashAttribute("successMessage", "Payment recorded.");
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/sales/invoices/" + id;
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    @Data
    public static class SalesInvoiceForm {
        private Long customerId, warehouseId;
        private LocalDate invoiceDate;
        private LocalDate dueDate;
        private String notes;
        private List<ItemForm> items = new ArrayList<>();
    }

    @Data
    public static class ItemForm {
        private Long productId;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountPct = BigDecimal.ZERO;
        private BigDecimal taxRate = BigDecimal.ZERO;
    }
}
