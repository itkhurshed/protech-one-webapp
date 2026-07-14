package com.protechone.controller;

import com.protechone.dto.admin.CompanyRequest;
import com.protechone.service.CompanyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public String view(Model model) {
        var company = companyService.getCurrent();
        CompanyForm form = new CompanyForm();
        form.setName(company.name()); form.setLegalName(company.legalName()); form.setTaxNumber(company.taxNumber());
        form.setCommercialRegistration(company.commercialRegistration()); form.setEmail(company.email());
        form.setPhone(company.phone()); form.setAddress(company.address()); form.setCity(company.city());
        form.setCountry(company.country()); form.setCurrencyCode(company.currencyCode());
        form.setLanguageCode(company.languageCode()); form.setFiscalYearStartMonth(company.fiscalYearStartMonth());
        model.addAttribute("form", form);
        model.addAttribute("branches", companyService.listBranches());
        return "settings/company";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN')")
    public String update(@ModelAttribute CompanyForm form, RedirectAttributes redirectAttributes) {
        companyService.update(form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "Company profile updated.");
        return "redirect:/company";
    }

    @PostMapping("/branches")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COMPANY_ADMIN')")
    public String createBranch(@RequestParam String name, @RequestParam(required = false) String code,
                                @RequestParam(required = false) String address, @RequestParam(required = false) String phone,
                                @RequestParam(required = false) Boolean isMain, RedirectAttributes redirectAttributes) {
        companyService.createBranch(new com.protechone.dto.admin.BranchRequest(name, code, address, phone, Boolean.TRUE.equals(isMain)));
        redirectAttributes.addFlashAttribute("successMessage", "Branch created.");
        return "redirect:/company";
    }

    @Data
    public static class CompanyForm {
        private String name, legalName, taxNumber, commercialRegistration, email, phone, address, city, country,
                currencyCode, languageCode, logoUrl;
        private Integer fiscalYearStartMonth;

        CompanyRequest toRequest() {
            return new CompanyRequest(name, legalName, taxNumber, commercialRegistration, email, phone, address,
                    city, country, currencyCode, languageCode, fiscalYearStartMonth, logoUrl);
        }
    }
}
