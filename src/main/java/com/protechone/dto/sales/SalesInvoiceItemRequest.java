package com.protechone.dto.sales;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SalesInvoiceItemRequest(
        @NotNull Long productId,
        @NotNull BigDecimal quantity,
        @NotNull BigDecimal unitPrice,
        BigDecimal discountPct,
        BigDecimal taxRate
) {}
