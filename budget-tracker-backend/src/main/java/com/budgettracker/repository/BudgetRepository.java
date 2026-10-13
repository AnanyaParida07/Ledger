package com.budgettracker.repository;

import com.budgettracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByTenantIdAndMonth(UUID tenantId, YearMonth month);
    Optional<Budget> findByTenantIdAndCategoryIdAndMonth(UUID tenantId, UUID categoryId, YearMonth month);
}
