package com.protechone.repository;

import com.protechone.entity.SalesInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalesInvoiceItemRepository extends JpaRepository<SalesInvoiceItem, Long> {

    @Query("SELECT i.product.id, i.product.name, SUM(i.quantity) as qty, SUM(i.lineTotal) as revenue " +
           "FROM SalesInvoiceItem i WHERE i.salesInvoice.company.id = :companyId " +
           "AND i.salesInvoice.status <> 'CANCELLED' " +
           "GROUP BY i.product.id, i.product.name ORDER BY revenue DESC")
    List<Object[]> topSellingProducts(@Param("companyId") Long companyId);
}
