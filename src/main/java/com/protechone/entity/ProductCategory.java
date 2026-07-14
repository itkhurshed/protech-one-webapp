package com.protechone.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductCategory parent;
}
