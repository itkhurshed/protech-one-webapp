package com.protechone.repository;

import com.protechone.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findByCompanyId(Long companyId);
}
