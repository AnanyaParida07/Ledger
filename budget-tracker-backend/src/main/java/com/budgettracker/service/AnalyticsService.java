package com.budgettracker.service;

import com.budgettracker.dto.CategorySpendResponse;
import com.budgettracker.dto.MonthlyTrendResponse;
import com.budgettracker.dto.SummaryResponse;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.repository.TransactionRepository;
import com.budgettracker.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analyticsSpendByCategory", key = "T(com.budgettracker.tenant.TenantContext).getTenantId().toString() + ':' + #month")
    public List<CategorySpendResponse> spendByCategory(YearMonth month) {
        UUID tenantId = currentTenantId();
        return transactionRepository.spendByCategoryInRange(
                tenantId,
                TransactionType.EXPENSE,
                month.atDay(1),
                month.atEndOfMonth()
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analyticsTrend", key = "T(com.budgettracker.tenant.TenantContext).getTenantId().toString() + ':' + #months")
    public List<MonthlyTrendResponse> monthlyTrend(int months) {
        if (months < 1 || months > 36) {
            throw new IllegalArgumentException("months must be between 1 and 36");
        }

        YearMonth firstMonth = YearMonth.now().minusMonths(months - 1L);
        return java.util.stream.IntStream.range(0, months)
                .mapToObj(offset -> trendForMonth(firstMonth.plusMonths(offset)))
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analyticsSummary", key = "T(com.budgettracker.tenant.TenantContext).getTenantId().toString() + ':' + #month")
    public SummaryResponse summary(YearMonth month) {
        BigDecimal income = sumForMonth(month, TransactionType.INCOME);
        BigDecimal expense = sumForMonth(month, TransactionType.EXPENSE);
        CategorySpendResponse topCategory = spendByCategory(month).stream().findFirst().orElse(null);

        return new SummaryResponse(
                month,
                income,
                expense,
                income.subtract(expense),
                topCategory != null ? topCategory.categoryName() : null,
                topCategory != null ? topCategory.totalAmount() : BigDecimal.ZERO
        );
    }

    private MonthlyTrendResponse trendForMonth(YearMonth month) {
        BigDecimal income = sumForMonth(month, TransactionType.INCOME);
        BigDecimal expense = sumForMonth(month, TransactionType.EXPENSE);
        return new MonthlyTrendResponse(month, income, expense, income.subtract(expense));
    }

    private BigDecimal sumForMonth(YearMonth month, TransactionType type) {
        UUID tenantId = currentTenantId();
        return transactionRepository.sumByTypeInRange(tenantId, type, month.atDay(1), month.atEndOfMonth());
    }

    private UUID currentTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Missing tenant context");
        }
        return tenantId;
    }
}
