package com.protechone.dto.sales;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SalesInvoiceResponse(
        Long id,
        String invoiceNumber,
        Long customerId,
        String customerName,
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
        List<SalesInvoiceItemResponse> items
) {}
