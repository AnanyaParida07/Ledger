package com.budgettracker.dto;

import com.budgettracker.entity.RecurrenceFrequency;
import com.budgettracker.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringTransactionResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        UUID createdById,
        BigDecimal amount,
        String description,
        TransactionType type,
        RecurrenceFrequency frequency,
        LocalDate nextRunDate,
        boolean active
) {
}
