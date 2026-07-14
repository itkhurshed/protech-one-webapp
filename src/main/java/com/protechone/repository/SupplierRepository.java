package com.protechone.repository;

import com.protechone.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Page<Supplier> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE s.company.id = :companyId AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           " LOWER(s.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           " LOWER(s.code) LIKE LOWER(CONCAT('%', :term, '%')))")
    Page<Supplier> search(@Param("companyId") Long companyId, @Param("term") String term, Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE s.company.id = :companyId AND " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Supplier> quickSearch(@Param("companyId") Long companyId, @Param("term") String term);

    long countByCompanyId(Long companyId);
}
