package com.budgettracker.service;

import com.budgettracker.dto.TransactionRequest;
import com.budgettracker.dto.TransactionResponse;
import com.budgettracker.dto.TransactionUpdateRequest;
import com.budgettracker.entity.Category;
import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.entity.User;
import com.budgettracker.repository.CategoryRepository;
import com.budgettracker.repository.TransactionRepository;
import com.budgettracker.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public List<TransactionResponse> listForMonth(YearMonth month) {
        UUID tenantId = currentTenantId();

        return transactionRepository
                .findByTenantIdAndDeletedFalseAndTransactionDateBetweenOrderByTransactionDateDesc(
                        tenantId,
                        month.atDay(1),
                        month.atEndOfMonth()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<Transaction> search(
            UUID tenantId,
            LocalDate startDate,
            LocalDate endDate,
            UUID categoryId,
            TransactionType type,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    ) {

        Specification<Transaction> filters = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("tenantId"), tenantId),
                criteriaBuilder.isFalse(root.get("deleted"))
        );

        if (startDate != null) {
            filters = filters.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
        }
        if (endDate != null) {
            filters = filters.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), endDate));
        }
        if (categoryId != null) {
            filters = filters.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("category").get("id"), categoryId));
        }
        if (type != null) {
            filters = filters.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("type"), type));
        }
        if (minAmount != null) {
            filters = filters.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount));
        }
        if (maxAmount != null) {
            filters = filters.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount));
        }

        return transactionRepository.findAll(filters, pageable);
    }

    @Transactional
    @CacheEvict(
            cacheNames = {
                    "analyticsSummary",
                    "analyticsTrend",
                    "analyticsSpendByCategory"
            },
            allEntries = true
    )
    public TransactionResponse create(TransactionRequest request) {

        UUID tenantId = currentTenantId();

        Category category = categoryRepository
                .findByIdAndTenantId(request.categoryId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Transaction transaction = new Transaction();

        transaction.setTenantId(tenantId);
        transaction.setCategory(category);
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setType(request.type());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setCreatedBy(currentUser());

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    @CacheEvict(
            cacheNames = {
                    "analyticsSummary",
                    "analyticsTrend",
                    "analyticsSpendByCategory"
            },
            allEntries = true
    )
    public void delete(UUID id) {

        Transaction transaction = transactionRepository
                .findByIdAndTenantId(id, currentTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        assertCanModify(transaction);

        transaction.setDeleted(true);

        transactionRepository.save(transaction);
    }

    @Transactional
    public TransactionResponse update(UUID id, TransactionUpdateRequest request) {

        Transaction transaction = transactionRepository
                .findByIdAndTenantId(id, currentTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        assertCanModify(transaction);

        transaction.setCategory(
                categoryRepository
                        .findByIdAndTenantId(request.categoryId(), currentTenantId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found"))
        );

        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setType(request.type());
        transaction.setTransactionDate(request.transactionDate());

        return toResponse(transactionRepository.save(transaction));
    }

    private User currentUser() {
        return (User) org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private void assertCanModify(Transaction transaction) {

        User actor = currentUser();

        if (actor.getRole() != com.budgettracker.entity.Role.ADMIN
                && (transaction.getCreatedBy() == null
                || !transaction.getCreatedBy().getId().equals(actor.getId()))) {

            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "You can only modify your own transaction"
            );
        }

        if (transaction.getTransactionDate().isBefore(LocalDate.now().minusDays(30))
                && actor.getRole() != com.budgettracker.entity.Role.ADMIN) {

            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Transactions older than 30 days can only be edited by admins"
            );
        }
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getCategory().getId(),
                t.getCategory().getName(),
                t.getAmount(),
                t.getDescription(),
                t.getType(),
                t.getTransactionDate()
        );
    }

    private UUID currentTenantId() {

        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new IllegalStateException("Missing tenant context");
        }

        return tenantId;
    }
}
