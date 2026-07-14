package com.protechone.dto.finance;

import jakarta.validation.constraints.NotBlank;

public record ExpenseCategoryRequest(@NotBlank String name) {}
