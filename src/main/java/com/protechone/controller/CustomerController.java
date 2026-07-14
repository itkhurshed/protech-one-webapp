package com.protechone.controller;

import com.protechone.dto.crm.CustomerRequest;
import com.protechone.service.CustomerService;
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
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        var result = customerService.list(search, PageRequest.of(page, 20, Sort.by("name").ascending()));
        model.addAttribute("customers", result.getContent());
        model.addAttribute("pageObj", result);
        model.addAttribute("search", search);
        return "crm/customers-list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','SALES_MANAGER','BRANCH_MANAGER')")
    public String newForm(Model model) {
        model.addAttribute("form", new CustomerForm());
        model.addAttribute("isEdit", false);
        return "crm/customer-form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','SALES_MANAGER','BRANCH_MANAGER')")
    public String create(@ModelAttribute CustomerForm form, RedirectAttributes redirectAttributes) {
        customerService.create(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Customer created successfully.");
        return "redirect:/customers";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','SALES_MANAGER','BRANCH_MANAGER')")
    public String editForm(@PathVariable Long id, Model model) {
        var customer = customerService.get(id);
        CustomerForm form = new CustomerForm();
        form.setCode(customer.code()); form.setName(customer.name()); form.setCategory(customer.category());
        form.setEmail(customer.email()); form.setPhone(customer.phone()); form.setWhatsapp(customer.whatsapp());
        form.setAddress(customer.address()); form.setCity(customer.city()); form.setCountry(customer.country());
        form.setTaxNumber(customer.taxNumber()); form.setCreditLimit(customer.creditLimit());
        form.setOpeningBalance(customer.openingBalance()); form.setNotes(customer.notes());
        form.setIsActive(customer.isActive());
        model.addAttribute("form", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("customerId", id);
        return "crm/customer-form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','SALES_MANAGER','BRANCH_MANAGER')")
    public String update(@PathVariable Long id, @ModelAttribute CustomerForm form, RedirectAttributes redirectAttributes) {
        customerService.update(id, form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully.");
        return "redirect:/customers";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN')")
    public String deleteConfirm(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.get(id));
        return "crm/customer-delete-confirm";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        customerService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Customer deleted.");
        return "redirect:/customers";
    }

    @Data
    public static class CustomerForm {
        private String code, name, category, email, phone, whatsapp, address, city, country, taxNumber, notes;
        private BigDecimal creditLimit = BigDecimal.ZERO;
        private BigDecimal openingBalance = BigDecimal.ZERO;
        private Boolean isActive = true;

        CustomerRequest toRequest() {
            return new CustomerRequest(code, name, category, email, phone, whatsapp, address, city, country,
                    taxNumber, creditLimit, openingBalance, notes, isActive);
        }
    }
}
