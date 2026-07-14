package com.protechone.service;

import com.protechone.dto.admin.BranchRequest;
import com.protechone.dto.admin.BranchResponse;
import com.protechone.dto.admin.CompanyRequest;
import com.protechone.dto.admin.CompanyResponse;
import com.protechone.entity.Branch;
import com.protechone.entity.Company;
import com.protechone.repository.BranchRepository;
import com.protechone.repository.CompanyRepository;
import com.protechone.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;
    private final CurrentUser currentUser;

    public CompanyResponse getCurrent() {
        return toResponse(currentUser.get().getCompany());
    }

    @Transactional
    public CompanyResponse update(CompanyRequest request) {
        Company company = currentUser.get().getCompany();
        company.setName(request.name());
        company.setLegalName(request.legalName());
        company.setTaxNumber(request.taxNumber());
        company.setCommercialRegistration(request.commercialRegistration());
        company.setEmail(request.email());
        company.setPhone(request.phone());
        company.setAddress(request.address());
        company.setCity(request.city());
        company.setCountry(request.country());
        if (request.currencyCode() != null) company.setCurrencyCode(request.currencyCode());
        if (request.languageCode() != null) company.setLanguageCode(request.languageCode());
        if (request.fiscalYearStartMonth() != null) company.setFiscalYearStartMonth(request.fiscalYearStartMonth());
        company.setLogoUrl(request.logoUrl());
        return toResponse(companyRepository.save(company));
    }

    public List<BranchResponse> listBranches() {
        return branchRepository.findByCompanyId(currentUser.companyId()).stream()
                .map(b -> new BranchResponse(b.getId(), b.getName(), b.getCode(), b.getAddress(), b.getPhone(), b.getIsMain(), b.getIsActive()))
                .toList();
    }

    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        Branch branch = branchRepository.save(Branch.builder()
                .company(currentUser.get().getCompany())
                .name(request.name())
                .code(request.code())
                .address(request.address())
                .phone(request.phone())
                .isMain(Boolean.TRUE.equals(request.isMain()))
                .isActive(true)
                .build());
        return new BranchResponse(branch.getId(), branch.getName(), branch.getCode(), branch.getAddress(),
                branch.getPhone(), branch.getIsMain(), branch.getIsActive());
    }

    private CompanyResponse toResponse(Company c) {
        return new CompanyResponse(c.getId(), c.getName(), c.getLegalName(), c.getTaxNumber(),
                c.getCommercialRegistration(), c.getEmail(), c.getPhone(), c.getAddress(), c.getCity(),
                c.getCountry(), c.getCurrencyCode(), c.getLanguageCode(), c.getFiscalYearStartMonth(), c.getLogoUrl());
    }
}
