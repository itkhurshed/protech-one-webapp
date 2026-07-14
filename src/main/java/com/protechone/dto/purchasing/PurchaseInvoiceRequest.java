package com.protechone.dto.purchasing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record PurchaseInvoiceRequest(
        @NotNull Long supplierId,
        Long warehouseId,
        LocalDate invoiceDate,
        LocalDate dueDate,
        String status,
        String notes,
        @NotEmpty @Valid List<PurchaseInvoiceItemRequest> items
) {}
