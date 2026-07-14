package com.protechone.dto.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        Long categoryId,
        String categoryName,
        LocalDate expenseDate,
        String referenceNo,
        String payee,
        BigDecimal amount,
        String paymentMethod,
        String notes
) {}
