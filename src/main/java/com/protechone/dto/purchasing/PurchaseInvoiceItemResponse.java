package com.protechone.dto.purchasing;

import java.math.BigDecimal;

public record PurchaseInvoiceItemResponse(
        Long id,
        Long productId,
        String productName,
        String sku,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal discountPct,
        BigDecimal taxRate,
        BigDecimal lineTotal
) {}
