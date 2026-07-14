package com.protechone.dto.inventory;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockTransferRequest(
        @NotNull Long productId,
        @NotNull Long fromWarehouseId,
        @NotNull Long toWarehouseId,
        @NotNull BigDecimal quantity,
        String notes
) {}
