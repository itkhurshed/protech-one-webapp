package com.protechone.dto.finance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        Long categoryId,
        LocalDate expenseDate,
        String referenceNo,
        String payee,
        @NotNull @Positive BigDecimal amount,
        String paymentMethod,
        String notes
) {}
