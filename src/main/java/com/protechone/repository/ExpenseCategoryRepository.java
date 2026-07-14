package com.protechone.repository;

import com.protechone.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    List<ExpenseCategory> findByCompanyId(Long companyId);
}
