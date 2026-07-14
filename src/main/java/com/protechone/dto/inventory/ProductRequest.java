package com.protechone.dto.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank String sku,
        String barcode,
        @NotBlank String name,
        String description,
        String brand,
        String unit,
        Long categoryId,
        @NotNull BigDecimal costPrice,
        @NotNull BigDecimal sellingPrice,
        BigDecimal taxRate,
        BigDecimal reorderLevel,
        BigDecimal minStock,
        BigDecimal maxStock,
        Boolean trackSerial,
        Boolean trackBatch,
        String imageUrl,
        Boolean isActive,
        Long warehouseId,
        BigDecimal openingQuantity
) {}
