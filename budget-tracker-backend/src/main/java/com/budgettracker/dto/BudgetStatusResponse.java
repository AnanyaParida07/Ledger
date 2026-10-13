package com.budgettracker.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record BudgetStatusResponse(
        UUID categoryId,
        String categoryName,
        YearMonth month,
        BigDecimal monthlyLimit,
        BigDecimal spent,
        BigDecimal remaining,
        int percentUsed,
        boolean overBudget
) {
}