package com.protechone.controller;

import com.protechone.dto.finance.ExpenseRequest;
import com.protechone.service.ExpenseService;
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
import java.time.LocalDate;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        var result = expenseService.list(PageRequest.of(page, 20, Sort.by("expenseDate").descending()));
        model.addAttribute("expenses", result.getContent());
        model.addAttribute("pageObj", result);
        model.addAttribute("categories", expenseService.listCategories());
        return "accounting/expenses-list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','ACCOUNTANT','BRANCH_MANAGER')")
    public String newForm(Model model) {
        ExpenseForm form = new ExpenseForm();
        form.setExpenseDate(LocalDate.now());
        model.addAttribute("form", form);
        model.addAttribute("categories", expenseService.listCategories());
        return "accounting/expense-form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','ACCOUNTANT','BRANCH_MANAGER')")
    public String create(@ModelAttribute ExpenseForm form, RedirectAttributes redirectAttributes) {
        expenseService.create(new ExpenseRequest(form.getCategoryId(), form.getExpenseDate(), form.getReferenceNo(),
                form.getPayee(), form.getAmount(), form.getPaymentMethod(), form.getNotes()));
        redirectAttributes.addFlashAttribute("successMessage", "Expense recorded.");
        return "redirect:/expenses";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','ACCOUNTANT')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        expenseService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Expense deleted.");
        return "redirect:/expenses";
    }

    @PostMapping("/category")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN','ACCOUNTANT')")
    public String createCategory(@RequestParam String name, RedirectAttributes redirectAttributes) {
        expenseService.createCategory(new com.protechone.dto.finance.ExpenseCategoryRequest(name));
        redirectAttributes.addFlashAttribute("successMessage", "Category created.");
        return "redirect:/expenses/new";
    }

    @Data
    public static class ExpenseForm {
        private Long categoryId;
        private LocalDate expenseDate;
        private String referenceNo, payee, paymentMethod = "CASH", notes;
        private BigDecimal amount;
    }
}
