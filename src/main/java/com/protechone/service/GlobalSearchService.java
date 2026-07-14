package com.protechone.service;

import com.protechone.dto.common.GlobalSearchResultResponse;
import com.protechone.repository.*;
import com.protechone.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Powers the Google-style homepage search: a single query fans out across
 * customers, suppliers, products, sales invoices and purchase invoices and
 * returns a unified, ranked-by-category result list.
 */
@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final CurrentUser currentUser;

    public List<GlobalSearchResultResponse> search(String term) {
        Long companyId = currentUser.companyId();
        List<GlobalSearchResultResponse> results = new ArrayList<>();

        customerRepository.quickSearch(companyId, term).forEach(c -> results.add(new GlobalSearchResultResponse(
                "Customer", c.getId(), c.getName(), c.getEmail() != null ? c.getEmail() : c.getPhone(),
                "/customers/" + c.getId())));

        supplierRepository.quickSearch(companyId, term).forEach(s -> results.add(new GlobalSearchResultResponse(
                "Supplier", s.getId(), s.getName(), s.getEmail() != null ? s.getEmail() : s.getPhone(),
                "/suppliers/" + s.getId())));

        productRepository.quickSearch(companyId, term).forEach(p -> results.add(new GlobalSearchResultResponse(
                "Product", p.getId(), p.getName(), "SKU: " + p.getSku(),
                "/products/" + p.getId())));

        salesInvoiceRepository.quickSearch(companyId, term).forEach(i -> results.add(new GlobalSearchResultResponse(
                "Sales Invoice", i.getId(), i.getInvoiceNumber(), i.getCustomer().getName(),
                "/sales/invoices/" + i.getId())));

        purchaseInvoiceRepository.quickSearch(companyId, term).forEach(i -> results.add(new GlobalSearchResultResponse(
                "Purchase Invoice", i.getId(), i.getInvoiceNumber(), i.getSupplier().getName(),
                "/purchasing/invoices/" + i.getId())));

        return results;
    }
}
