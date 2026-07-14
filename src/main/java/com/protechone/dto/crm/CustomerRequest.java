package com.protechone.dto.crm;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CustomerRequest(
        String code,
        @NotBlank String name,
        String category,
        String email,
        String phone,
        String whatsapp,
        String address,
        String city,
        String country,
        String taxNumber,
        BigDecimal creditLimit,
        BigDecimal openingBalance,
        String notes,
        Boolean isActive
) {}
