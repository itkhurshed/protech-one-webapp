package com.protechone.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_invoice_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_invoice_id", nullable = false)
    private SalesInvoice salesInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_pct")
    private BigDecimal discountPct;

    @Column(name = "tax_rate")
    private BigDecimal taxRate;

    @Column(name = "line_total", nullable = false)
    private BigDecimal lineTotal;
}
