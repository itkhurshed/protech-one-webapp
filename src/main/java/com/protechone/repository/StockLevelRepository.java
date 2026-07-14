package com.protechone.repository;

import com.protechone.entity.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {

    Optional<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<StockLevel> findByProductId(Long productId);

    @Query("SELECT COALESCE(SUM(sl.quantity), 0) FROM StockLevel sl WHERE sl.product.id = :productId")
    java.math.BigDecimal totalQuantityForProduct(@Param("productId") Long productId);

    @Query("SELECT sl FROM StockLevel sl WHERE sl.product.company.id = :companyId AND " +
           "sl.quantity <= sl.product.reorderLevel")
    List<StockLevel> findLowStock(@Param("companyId") Long companyId);

    @Query("SELECT COALESCE(SUM(sl.quantity * sl.product.costPrice), 0) FROM StockLevel sl WHERE sl.product.company.id = :companyId")
    java.math.BigDecimal totalInventoryValue(@Param("companyId") Long companyId);
}
