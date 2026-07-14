package com.protechone.controller;

import com.protechone.dto.crm.SupplierRequest;
import com.protechone.service.SupplierService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        var result = supplierService.list(search, PageRequest.of(page, 20, Sort.by("name").ascending()));
        model.addAttribute("suppliers", result.getContent());
        model.addAttribute("pageObj", result);
        model.addAttribute("search", search);
        return "purchasing/suppliers-list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER','BRANCH_MANAGER')")
    public String newForm(Model model) {
        model.addAttribute("form", new SupplierForm());
        model.addAttribute("isEdit", false);
        return "purchasing/supplier-form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER','BRANCH_MANAGER')")
    public String create(@ModelAttribute SupplierForm form, RedirectAttributes redirectAttributes) {
        supplierService.create(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Supplier created successfully.");
        return "redirect:/suppliers";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER','BRANCH_MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        var s = supplierService.get(id);
        SupplierForm form = new SupplierForm();
        form.setCode(s.code()); form.setName(s.name()); form.setEmail(s.email()); form.setPhone(s.phone());
        form.setAddress(s.address()); form.setCity(s.city()); form.setCountry(s.country());
        form.setTaxNumber(s.taxNumber()); form.setOpeningBalance(s.openingBalance());
        form.setPaymentTerms(s.paymentTerms()); form.setNotes(s.notes()); form.setIsActive(s.isActive());
        model.addAttribute("form", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("supplierId", id);
        return "purchasing/supplier-form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','WAREHOUSE_MANAGER','BRANCH_MANAGER')")
    public String update(@PathVariable Long id, @ModelAttribute SupplierForm form, RedirectAttributes redirectAttributes) {
        supplierService.update(id, form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Supplier updated successfully.");
        return "redirect:/suppliers";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN')")
    public String deleteConfirm(@PathVariable Long id, Model model) {
        model.addAttribute("supplier", supplierService.get(id));
        return "purchasing/supplier-delete-confirm";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        supplierService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Supplier deleted.");
        return "redirect:/suppliers";
    }

    @Data
    public static class SupplierForm {
        private String code, name, email, phone, address, city, country, taxNumber, paymentTerms, notes;
        private BigDecimal openingBalance = BigDecimal.ZERO;
        private Boolean isActive = true;

        SupplierRequest toRequest() {
            return new SupplierRequest(code, name, email, phone, address, city, country, taxNumber,
                    openingBalance, paymentTerms, notes, isActive);
        }
    }
}
