package com.protechone.repository;

import com.protechone.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByCompanyId(Long companyId);
}
