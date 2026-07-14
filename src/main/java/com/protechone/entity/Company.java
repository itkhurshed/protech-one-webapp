package com.protechone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "legal_name", length = 200)
    private String legalName;

    @Column(name = "tax_number", length = 100)
    private String taxNumber;

    @Column(name = "commercial_registration", length = 100)
    private String commercialRegistration;

    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "currency_code")
    private String currencyCode;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "fiscal_year_start_month")
    private Integer fiscalYearStartMonth;

    @Column(name = "is_active")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
