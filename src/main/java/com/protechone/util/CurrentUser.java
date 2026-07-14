package com.protechone.util;

import com.protechone.entity.User;
import com.protechone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the authenticated User entity (and therefore the tenant/company
 * scope) for the current request from the Spring Security context.
 * Every service uses this to scope queries to the caller's company,
 * enforcing multi-tenant data isolation.
 */
@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UserRepository userRepository;

    public User get() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new com.protechone.exception.ResourceNotFoundException("Authenticated user not found"));
    }

    public Long companyId() {
        return get().getCompany().getId();
    }
}
