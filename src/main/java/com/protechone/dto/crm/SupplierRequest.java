package com.protechone.dto.crm;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record SupplierRequest(
        String code,
        @NotBlank String name,
        String email,
        String phone,
        String address,
        String city,
        String country,
        String taxNumber,
        BigDecimal openingBalance,
        String paymentTerms,
        String notes,
        Boolean isActive
) {}
