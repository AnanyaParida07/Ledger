package com.budgettracker.dto;

import java.math.BigDecimal;

public record CategorySpendResponse(
        String categoryName,
        BigDecimal totalAmount
) {
}
