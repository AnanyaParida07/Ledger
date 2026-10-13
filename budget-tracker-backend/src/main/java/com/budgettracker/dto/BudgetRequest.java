package com.budgettracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record BudgetRequest(
        @NotNull UUID categoryId,
        @NotNull YearMonth month,
        @NotNull @Positive BigDecimal monthlyLimit
) {
}