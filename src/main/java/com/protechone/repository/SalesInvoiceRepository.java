package com.protechone.repository;

import com.protechone.entity.SalesInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {

    Page<SalesInvoice> findByCompanyIdOrderByInvoiceDateDesc(Long companyId, Pageable pageable);

    @Query("SELECT si FROM SalesInvoice si WHERE si.company.id = :companyId AND " +
           "LOWER(si.invoiceNumber) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<SalesInvoice> quickSearch(@Param("companyId") Long companyId, @Param("term") String term);

    long countByCompanyId(Long companyId);

    @Query("SELECT COALESCE(SUM(si.grandTotal), 0) FROM SalesInvoice si WHERE si.company.id = :companyId " +
           "AND si.invoiceDate = :date AND si.status <> 'CANCELLED'")
    BigDecimal totalSalesForDate(@Param("companyId") Long companyId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(si.grandTotal), 0) FROM SalesInvoice si WHERE si.company.id = :companyId " +
           "AND si.invoiceDate BETWEEN :start AND :end AND si.status <> 'CANCELLED'")
    BigDecimal totalSalesBetween(@Param("companyId") Long companyId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(si.grandTotal - si.amountPaid), 0) FROM SalesInvoice si WHERE si.company.id = :companyId " +
           "AND si.status IN ('CONFIRMED','PARTIALLY_PAID','OVERDUE')")
    BigDecimal totalOutstanding(@Param("companyId") Long companyId);

    @Query("SELECT FUNCTION('to_char', si.invoiceDate, 'YYYY-MM') as month, SUM(si.grandTotal) as total " +
           "FROM SalesInvoice si WHERE si.company.id = :companyId AND si.status <> 'CANCELLED' " +
           "AND si.invoiceDate >= :since GROUP BY FUNCTION('to_char', si.invoiceDate, 'YYYY-MM') ORDER BY 1")
    List<Object[]> monthlySalesSince(@Param("companyId") Long companyId, @Param("since") LocalDate since);

    List<SalesInvoice> findTop10ByCompanyIdOrderByCreatedAtDesc(Long companyId);
}
