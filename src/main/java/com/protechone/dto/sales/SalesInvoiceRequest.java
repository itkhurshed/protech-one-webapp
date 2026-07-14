package com.protechone.dto.sales;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record SalesInvoiceRequest(
        @NotNull Long customerId,
        Long warehouseId,
        LocalDate invoiceDate,
        LocalDate dueDate,
        String status,
        String notes,
        @NotEmpty @Valid List<SalesInvoiceItemRequest> items
) {}
