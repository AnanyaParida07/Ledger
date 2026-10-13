package com.budgettracker.repository;

import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.dto.CategorySpendResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByTenantIdAndDeletedFalseAndTransactionDateBetweenOrderByTransactionDateDesc(UUID tenantId, LocalDate start, LocalDate end);

    Optional<Transaction> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.tenantId = :tenantId AND t.category.id = :categoryId AND t.type = 'EXPENSE'
            AND t.transactionDate BETWEEN :start AND :end
            """)
    BigDecimal sumExpensesForCategoryInRange(@Param("tenantId") UUID tenantId, @Param("categoryId") UUID categoryId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("""
            SELECT new com.budgettracker.dto.CategorySpendResponse(t.category.name, COALESCE(SUM(t.amount), 0))
            FROM Transaction t
            WHERE t.tenantId = :tenantId AND t.type = :type
            AND t.transactionDate BETWEEN :start AND :end
            GROUP BY t.category.name
            ORDER BY COALESCE(SUM(t.amount), 0) DESC
            """)
    List<CategorySpendResponse> spendByCategoryInRange(@Param("tenantId") UUID tenantId, @Param("type") TransactionType type, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.tenantId = :tenantId AND t.type = :type
            AND t.transactionDate BETWEEN :start AND :end
            """)
    BigDecimal sumByTypeInRange(@Param("tenantId") UUID tenantId, @Param("type") TransactionType type, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
