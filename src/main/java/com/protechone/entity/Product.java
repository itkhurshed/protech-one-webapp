package com.protechone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @Column(nullable = false, length = 60)
    private String sku;

    private String barcode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String brand;
    private String unit;

    @Column(name = "cost_price")
    private BigDecimal costPrice;

    @Column(name = "selling_price")
    private BigDecimal sellingPrice;

    @Column(name = "tax_rate")
    private BigDecimal taxRate;

    @Column(name = "reorder_level")
    private BigDecimal reorderLevel;

    @Column(name = "min_stock")
    private BigDecimal minStock;

    @Column(name = "max_stock")
    private BigDecimal maxStock;

    @Column(name = "track_serial")
    private Boolean trackSerial;

    @Column(name = "track_batch")
    private Boolean trackBatch;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
