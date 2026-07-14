package com.protechone.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record BranchRequest(@NotBlank String name, String code, String address, String phone, Boolean isMain) {}
