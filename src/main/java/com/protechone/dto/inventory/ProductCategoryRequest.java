package com.protechone.dto.inventory;

import jakarta.validation.constraints.NotBlank;

public record ProductCategoryRequest(@NotBlank String name, Long parentId) {}
