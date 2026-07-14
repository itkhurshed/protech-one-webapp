package com.protechone.repository;

import com.protechone.entity.PurchaseInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseInvoiceItemRepository extends JpaRepository<PurchaseInvoiceItem, Long> {
}
