package com.protechone.dto.sales;

import java.math.BigDecimal;

public record SalesInvoiceItemResponse(
        Long id,
        Long productId,
        String productName,
        String sku,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal discountPct,
        BigDecimal taxRate,
        BigDecimal lineTotal
) {}
