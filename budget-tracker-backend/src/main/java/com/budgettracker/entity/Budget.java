package com.budgettracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(
        name = "budgets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "category_id", "budget_month"})
)
@Getter
@Setter
@NoArgsConstructor
public class Budget extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // A YearMonthAttributeConverter (below) stores this as "2026-08".
    @Column(name = "budget_month", nullable = false)
    private YearMonth month;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyLimit;
}