package com.protechone.dto.inventory;

import jakarta.validation.constraints.NotBlank;

public record WarehouseRequest(@NotBlank String name, String code, String location, Long branchId) {}
