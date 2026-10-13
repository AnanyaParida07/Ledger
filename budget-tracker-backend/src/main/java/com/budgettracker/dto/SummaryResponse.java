package com.budgettracker.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record SummaryResponse(
        YearMonth month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal net,
        String topCategory,
        BigDecimal topCategorySpend
) {
}
