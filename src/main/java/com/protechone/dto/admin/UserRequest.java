package com.protechone.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        String employeeNumber,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        String phone,
        String password,
        @NotNull Long roleId,
        Long branchId,
        Boolean isActive
) {}
