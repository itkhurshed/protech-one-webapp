package com.protechone.dto.crm;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String code,
        String name,
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
        Boolean isActive,
        LocalDateTime createdAt
) {}
