package com.budgettracker.dto;

import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        BigDecimal amount,
        String description,
        TransactionType type,
        LocalDate transactionDate
) {

    public static TransactionResponse fromEntity(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getCategory() != null ? t.getCategory().getName() : null,
                t.getAmount(),
                t.getDescription(),
                t.getType(),
                t.getTransactionDate()
        );
    }
}