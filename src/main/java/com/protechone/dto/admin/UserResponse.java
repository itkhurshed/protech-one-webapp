package com.protechone.dto.admin;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String employeeNumber,
        String firstName,
        String lastName,
        String email,
        String phone,
        Long roleId,
        String roleName,
        Boolean isActive,
        Boolean isLocked,
        Boolean mfaEnabled,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {}
