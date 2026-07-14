package com.protechone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_invoices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "invoice_number", nullable = false, length = 40)
    private String invoiceNumber;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false, length = 20)
    private String status; // DRAFT, CONFIRMED, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED

    private BigDecimal subtotal;

    @Column(name = "discount_total")
    private BigDecimal discountTotal;

    @Column(name = "tax_total")
    private BigDecimal taxTotal;

    @Column(name = "grand_total")
    private BigDecimal grandTotal;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "salesInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesInvoiceItem> items = new ArrayList<>();
}
