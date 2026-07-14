package com.protechone.dto.purchasing;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseInvoiceItemRequest(
        @NotNull Long productId,
        @NotNull BigDecimal quantity,
        @NotNull BigDecimal unitCost,
        BigDecimal discountPct,
        BigDecimal taxRate
) {}
