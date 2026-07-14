package com.protechone.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "expense_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExpenseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 120)
    private String name;
}
