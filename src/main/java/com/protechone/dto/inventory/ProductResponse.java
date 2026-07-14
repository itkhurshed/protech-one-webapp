package com.protechone.dto.inventory;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String barcode,
        String name,
        String description,
        String brand,
        String unit,
        Long categoryId,
        String categoryName,
        BigDecimal costPrice,
        BigDecimal sellingPrice,
        BigDecimal taxRate,
        BigDecimal reorderLevel,
        BigDecimal minStock,
        BigDecimal maxStock,
        BigDecimal totalStock,
        String stockStatus,
        Boolean isActive
) {}
