package com.protechone.dto.crm;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SupplierResponse(
        Long id,
        String code,
        String name,
        String email,
        String phone,
        String address,
        String city,
        String country,
        String taxNumber,
        BigDecimal openingBalance,
        String paymentTerms,
        String notes,
        Boolean isActive,
        LocalDateTime createdAt
) {}
