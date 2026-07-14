package com.protechone.service;

import com.protechone.dto.crm.CustomerRequest;
import com.protechone.dto.crm.CustomerResponse;
import com.protechone.entity.Company;
import com.protechone.entity.Customer;
import com.protechone.exception.ResourceNotFoundException;
import com.protechone.repository.CustomerRepository;
import com.protechone.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CurrentUser currentUser;

    public Page<CustomerResponse> list(String search, Pageable pageable) {
        Long companyId = currentUser.companyId();
        Page<Customer> page = (search == null || search.isBlank())
                ? customerRepository.findByCompanyId(companyId, pageable)
                : customerRepository.search(companyId, search, pageable);
        return page.map(this::toResponse);
    }

    public CustomerResponse get(Long id) {
        return toResponse(findOwned(id));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        Company company = currentUser.get().getCompany();
        Customer customer = Customer.builder()
                .company(company)
                .code(request.code())
                .name(request.name())
                .category(request.category())
                .email(request.email())
                .phone(request.phone())
                .whatsapp(request.whatsapp())
                .address(request.address())
                .city(request.city())
                .country(request.country())
                .taxNumber(request.taxNumber())
                .creditLimit(request.creditLimit())
                .openingBalance(request.openingBalance())
                .notes(request.notes())
                .isActive(request.isActive() == null || request.isActive())
                .createdBy(currentUser.get())
                .build();
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findOwned(id);
        customer.setCode(request.code());
        customer.setName(request.name());
        customer.setCategory(request.category());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setWhatsapp(request.whatsapp());
        customer.setAddress(request.address());
        customer.setCity(request.city());
        customer.setCountry(request.country());
        customer.setTaxNumber(request.taxNumber());
        customer.setCreditLimit(request.creditLimit());
        customer.setOpeningBalance(request.openingBalance());
        customer.setNotes(request.notes());
        if (request.isActive() != null) customer.setIsActive(request.isActive());
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = findOwned(id);
        customerRepository.delete(customer);
    }

    private Customer findOwned(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        if (!customer.getCompany().getId().equals(currentUser.companyId())) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }
        return customer;
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId(), c.getCode(), c.getName(), c.getCategory(), c.getEmail(),
                c.getPhone(), c.getWhatsapp(), c.getAddress(), c.getCity(), c.getCountry(), c.getTaxNumber(),
                c.getCreditLimit(), c.getOpeningBalance(), c.getNotes(), c.getIsActive(), c.getCreatedAt());
    }
}
