package com.budgettracker.service;

import com.budgettracker.dto.BudgetRequest;
import com.budgettracker.dto.BudgetStatusResponse;
import com.budgettracker.dto.BudgetUpdateRequest;
import com.budgettracker.entity.Budget;
import com.budgettracker.entity.Category;
import com.budgettracker.repository.BudgetRepository;
import com.budgettracker.repository.CategoryRepository;
import com.budgettracker.repository.TransactionRepository;
import com.budgettracker.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public BudgetStatusResponse setBudget(BudgetRequest request) {
        UUID tenantId = currentTenantId();
        Category category = categoryRepository
                .findByIdAndTenantId(request.categoryId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Budget budget = budgetRepository
                .findByTenantIdAndCategoryIdAndMonth(tenantId, request.categoryId(), request.month())
                .orElseGet(Budget::new);

        budget.setTenantId(tenantId);
        budget.setCategory(category);
        budget.setMonth(request.month());
        budget.setMonthlyLimit(request.monthlyLimit());

        return toStatus(budgetRepository.save(budget));
    }

    public List<BudgetStatusResponse> statusForMonth(YearMonth month) {
        return budgetRepository.findByTenantIdAndMonth(currentTenantId(), month).stream().map(this::toStatus).toList();
    }

    @Transactional
    public BudgetStatusResponse updateBudget(UUID categoryId, YearMonth month, BudgetUpdateRequest request) {
        Budget budget = budgetRepository
                .findByTenantIdAndCategoryIdAndMonth(currentTenantId(), categoryId, month)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found"));
        budget.setMonthlyLimit(request.monthlyLimit());
        return toStatus(budgetRepository.save(budget));
    }

    @Transactional
    public void deleteBudget(UUID categoryId, YearMonth month) {
        Budget budget = budgetRepository
                .findByTenantIdAndCategoryIdAndMonth(currentTenantId(), categoryId, month)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found"));
        budgetRepository.delete(budget);
    }

    private BudgetStatusResponse toStatus(Budget budget) {
        BigDecimal spent = transactionRepository.sumExpensesForCategoryInRange(
                budget.getTenantId(),
                budget.getCategory().getId(),
                budget.getMonth().atDay(1),
                budget.getMonth().atEndOfMonth()
        );
        BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
        int percentUsed = budget.getMonthlyLimit().signum() == 0
                ? 0
                : spent.multiply(BigDecimal.valueOf(100))
                .divide(budget.getMonthlyLimit(), 0, RoundingMode.HALF_UP)
                .intValue();

        return new BudgetStatusResponse(
                budget.getCategory().getId(), budget.getCategory().getName(), budget.getMonth(),
                budget.getMonthlyLimit(), spent, remaining, percentUsed, remaining.signum() < 0
        );
    }

    private UUID currentTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Missing tenant context");
        }
        return tenantId;
    }
}
