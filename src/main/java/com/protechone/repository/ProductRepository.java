package com.protechone.repository;

import com.protechone.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.company.id = :companyId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           " LOWER(p.sku) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           " LOWER(p.barcode) LIKE LOWER(CONCAT('%', :term, '%')))")
    Page<Product> search(@Param("companyId") Long companyId, @Param("term") String term, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.company.id = :companyId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Product> quickSearch(@Param("companyId") Long companyId, @Param("term") String term);

    long countByCompanyId(Long companyId);

    boolean existsByCompanyIdAndSkuIgnoreCase(Long companyId, String sku);
}
