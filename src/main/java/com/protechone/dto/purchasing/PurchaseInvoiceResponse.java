package com.protechone.dto.purchasing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseInvoiceResponse(
        Long id,
        String invoiceNumber,
        Long supplierId,
        String supplierName,
        LocalDate invoiceDate,
        LocalDate dueDate,
        String status,
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal taxTotal,
        BigDecimal grandTotal,
        BigDecimal amountPaid,
        BigDecimal balanceDue,
        String notes,
        LocalDateTime createdAt,
        List<PurchaseInvoiceItemResponse> items
) {}
