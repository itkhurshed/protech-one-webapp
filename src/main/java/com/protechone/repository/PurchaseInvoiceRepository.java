package com.protechone.repository;

import com.protechone.entity.PurchaseInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {

    Page<PurchaseInvoice> findByCompanyIdOrderByInvoiceDateDesc(Long companyId, Pageable pageable);

    @Query("SELECT pi FROM PurchaseInvoice pi WHERE pi.company.id = :companyId AND " +
           "LOWER(pi.invoiceNumber) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<PurchaseInvoice> quickSearch(@Param("companyId") Long companyId, @Param("term") String term);

    long countByCompanyId(Long companyId);

    @Query("SELECT COALESCE(SUM(pi.grandTotal), 0) FROM PurchaseInvoice pi WHERE pi.company.id = :companyId " +
           "AND pi.invoiceDate BETWEEN :start AND :end AND pi.status <> 'CANCELLED'")
    BigDecimal totalPurchasesBetween(@Param("companyId") Long companyId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(pi.grandTotal - pi.amountPaid), 0) FROM PurchaseInvoice pi WHERE pi.company.id = :companyId " +
           "AND pi.status IN ('CONFIRMED','PARTIALLY_PAID','OVERDUE')")
    BigDecimal totalOutstandingPayable(@Param("companyId") Long companyId);
}
