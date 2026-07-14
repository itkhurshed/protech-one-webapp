package com.protechone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    private String category;
    private String email;
    private String phone;
    private String whatsapp;
    private String address;
    private String city;
    private String country;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;

    @Column(name = "opening_balance")
    private BigDecimal openingBalance;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
