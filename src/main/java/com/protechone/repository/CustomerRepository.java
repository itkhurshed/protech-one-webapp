package com.protechone.repository;

import com.protechone.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.company.id = :companyId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           " LOWER(c.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           " LOWER(c.code) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           " LOWER(c.phone) LIKE LOWER(CONCAT('%', :term, '%')))")
    Page<Customer> search(@Param("companyId") Long companyId, @Param("term") String term, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.company.id = :companyId AND " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Customer> quickSearch(@Param("companyId") Long companyId, @Param("term") String term);

    long countByCompanyId(Long companyId);
}
