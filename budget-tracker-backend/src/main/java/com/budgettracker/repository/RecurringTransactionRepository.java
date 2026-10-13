package com.budgettracker.repository;

import com.budgettracker.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {
    List<RecurringTransaction> findByTenantId(UUID tenantId);
    Optional<RecurringTransaction> findByIdAndTenantId(UUID id, UUID tenantId);

    // Deliberately NOT tenant-scoped - the scheduler needs due rows across
    // every tenant in one pass. It runs with no Hibernate filter enabled
    // anyway (no HTTP request, no JWT), so this returns everyone's due rows.
    List<RecurringTransaction> findByActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
}
