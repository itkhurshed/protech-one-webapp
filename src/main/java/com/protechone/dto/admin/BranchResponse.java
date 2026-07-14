package com.protechone.dto.admin;

public record BranchResponse(Long id, String name, String code, String address, String phone, Boolean isMain, Boolean isActive) {}
