package com.protechone.repository;

import com.protechone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByCompanyId(Long companyId);
    long countByCompanyId(Long companyId);
    Optional<User> findByPasswordResetToken(String token);
    Optional<User> findByEmailVerifyToken(String token);
}
