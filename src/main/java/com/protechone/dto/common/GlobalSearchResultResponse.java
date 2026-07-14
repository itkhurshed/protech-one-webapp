package com.protechone.dto.common;

public record GlobalSearchResultResponse(
        String category,   // Customer, Supplier, Product, Sales Invoice, Purchase Invoice
        Long id,
        String title,
        String subtitle,
        String url
) {}
