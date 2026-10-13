package com.budgettracker.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyTrendResponse(
        YearMonth month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
}
