package com.protechone.repository;

import com.protechone.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByCompanyIdOrderByExpenseDateDesc(Long companyId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.company.id = :companyId " +
           "AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal totalExpensesBetween(@Param("companyId") Long companyId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
