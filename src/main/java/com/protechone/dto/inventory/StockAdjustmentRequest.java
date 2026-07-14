package com.protechone.dto.inventory;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockAdjustmentRequest(
        @NotNull Long productId,
        @NotNull Long warehouseId,
        @NotNull BigDecimal quantity,
        @NotNull String movementType, // IN, OUT, ADJUSTMENT
        String notes
) {}
