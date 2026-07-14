package com.protechone.service;

import com.protechone.dto.crm.SupplierRequest;
import com.protechone.dto.crm.SupplierResponse;
import com.protechone.entity.Company;
import com.protechone.entity.Supplier;
import com.protechone.exception.ResourceNotFoundException;
import com.protechone.repository.SupplierRepository;
import com.protechone.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final CurrentUser currentUser;

    public Page<SupplierResponse> list(String search, Pageable pageable) {
        Long companyId = currentUser.companyId();
        Page<Supplier> page = (search == null || search.isBlank())
                ? supplierRepository.findByCompanyId(companyId, pageable)
                : supplierRepository.search(companyId, search, pageable);
        return page.map(this::toResponse);
    }

    public SupplierResponse get(Long id) {
        return toResponse(findOwned(id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Company company = currentUser.get().getCompany();
        Supplier supplier = Supplier.builder()
                .company(company)
                .code(request.code())
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .city(request.city())
                .country(request.country())
                .taxNumber(request.taxNumber())
                .openingBalance(request.openingBalance())
                .paymentTerms(request.paymentTerms())
                .notes(request.notes())
                .isActive(request.isActive() == null || request.isActive())
                .createdBy(currentUser.get())
                .build();
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = findOwned(id);
        supplier.setCode(request.code());
        supplier.setName(request.name());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
        supplier.setAddress(request.address());
        supplier.setCity(request.city());
        supplier.setCountry(request.country());
        supplier.setTaxNumber(request.taxNumber());
        supplier.setOpeningBalance(request.openingBalance());
        supplier.setPaymentTerms(request.paymentTerms());
        supplier.setNotes(request.notes());
        if (request.isActive() != null) supplier.setIsActive(request.isActive());
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void delete(Long id) {
        supplierRepository.delete(findOwned(id));
    }

    private Supplier findOwned(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
        if (!supplier.getCompany().getId().equals(currentUser.companyId())) {
            throw new ResourceNotFoundException("Supplier not found: " + id);
        }
        return supplier;
    }

    private SupplierResponse toResponse(Supplier s) {
        return new SupplierResponse(s.getId(), s.getCode(), s.getName(), s.getEmail(), s.getPhone(),
                s.getAddress(), s.getCity(), s.getCountry(), s.getTaxNumber(), s.getOpeningBalance(),
                s.getPaymentTerms(), s.getNotes(), s.getIsActive(), s.getCreatedAt());
    }
}
