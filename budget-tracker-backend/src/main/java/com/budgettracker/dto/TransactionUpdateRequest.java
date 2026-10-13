package com.budgettracker.dto;

import com.budgettracker.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionUpdateRequest(@NotNull UUID categoryId, @Positive BigDecimal amount, String description,
                                       @NotNull TransactionType type, @NotNull LocalDate transactionDate, String editReason) {}
