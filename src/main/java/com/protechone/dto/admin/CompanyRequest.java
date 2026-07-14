package com.protechone.dto.admin;

public record CompanyRequest(
        String name,
        String legalName,
        String taxNumber,
        String commercialRegistration,
        String email,
        String phone,
        String address,
        String city,
        String country,
        String currencyCode,
        String languageCode,
        Integer fiscalYearStartMonth,
        String logoUrl
) {}
