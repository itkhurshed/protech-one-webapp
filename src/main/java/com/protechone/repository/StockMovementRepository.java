package com.protechone.repository;

import com.protechone.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);
    Page<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
}
