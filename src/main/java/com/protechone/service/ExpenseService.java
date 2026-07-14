package com.protechone.service;

import com.protechone.dto.finance.*;
import com.protechone.entity.Expense;
import com.protechone.entity.ExpenseCategory;
import com.protechone.exception.ResourceNotFoundException;
import com.protechone.repository.ExpenseCategoryRepository;
import com.protechone.repository.ExpenseRepository;
import com.protechone.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final CurrentUser currentUser;

    public List<ExpenseCategoryResponse> listCategories() {
        return categoryRepository.findByCompanyId(currentUser.companyId()).stream()
                .map(c -> new ExpenseCategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    @Transactional
    public ExpenseCategoryResponse createCategory(ExpenseCategoryRequest request) {
        ExpenseCategory saved = categoryRepository.save(ExpenseCategory.builder()
                .company(currentUser.get().getCompany())
                .name(request.name())
                .build());
        return new ExpenseCategoryResponse(saved.getId(), saved.getName());
    }

    public Page<ExpenseResponse> list(Pageable pageable) {
        return expenseRepository.findByCompanyIdOrderByExpenseDateDesc(currentUser.companyId(), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        ExpenseCategory category = request.categoryId() != null ? categoryRepository.findById(request.categoryId()).orElse(null) : null;
        Expense expense = expenseRepository.save(Expense.builder()
                .company(currentUser.get().getCompany())
                .category(category)
                .expenseDate(request.expenseDate() != null ? request.expenseDate() : LocalDate.now())
                .referenceNo(request.referenceNo())
                .payee(request.payee())
                .amount(request.amount())
                .paymentMethod(request.paymentMethod() != null ? request.paymentMethod() : "CASH")
                .notes(request.notes())
                .createdBy(currentUser.get())
                .build());
        return toResponse(expense);
    }

    @Transactional
    public void delete(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        if (!expense.getCompany().getId().equals(currentUser.companyId())) {
            throw new ResourceNotFoundException("Expense not found: " + id);
        }
        expenseRepository.delete(expense);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getCategory() != null ? e.getCategory().getId() : null,
                e.getCategory() != null ? e.getCategory().getName() : null, e.getExpenseDate(), e.getReferenceNo(),
                e.getPayee(), e.getAmount(), e.getPaymentMethod(), e.getNotes());
    }
}
