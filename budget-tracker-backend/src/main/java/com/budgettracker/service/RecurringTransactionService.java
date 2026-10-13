package com.budgettracker.service;

import com.budgettracker.dto.RecurringTransactionRequest;
import com.budgettracker.dto.RecurringTransactionResponse;
import com.budgettracker.dto.RecurringTransactionUpdateRequest;
import com.budgettracker.entity.Category;
import com.budgettracker.entity.Role;
import com.budgettracker.entity.RecurringTransaction;
import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.User;
import com.budgettracker.repository.CategoryRepository;
import com.budgettracker.repository.RecurringTransactionRepository;
import com.budgettracker.repository.TransactionRepository;
import com.budgettracker.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public RecurringTransactionResponse create(RecurringTransactionRequest request) {
        UUID tenantId = currentTenantId();
        Category category = categoryRepository
                .findByIdAndTenantId(request.categoryId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setTenantId(tenantId);
        recurring.setCategory(category);
        recurring.setAmount(request.amount());
        recurring.setDescription(request.description());
        recurring.setType(request.type());
        recurring.setFrequency(request.frequency());
        recurring.setNextRunDate(request.startDate());
        recurring.setActive(true);
        recurring.setCreatedBy(currentUser());

        return toResponse(recurringTransactionRepository.save(recurring));
    }

    public List<RecurringTransactionResponse> listAll() {
        return recurringTransactionRepository.findByTenantId(currentTenantId()).stream().map(this::toResponse).toList();
    }

    private RecurringTransactionResponse toResponse(RecurringTransaction r) {
        return new RecurringTransactionResponse(
                r.getId(), r.getCategory().getId(), r.getCategory().getName(),
                r.getCreatedBy() != null ? r.getCreatedBy().getId() : null,
                r.getAmount(), r.getDescription(), r.getType(), r.getFrequency(),
                r.getNextRunDate(), r.isActive()
        );
    }

    @Transactional
    public void setActive(UUID id, boolean active) {
        RecurringTransaction recurring = recurringTransactionRepository.findByIdAndTenantId(id, currentTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Recurring transaction not found"));
        assertCanModify(recurring);
        recurring.setActive(active);
    }

    @Transactional
    public RecurringTransactionResponse update(UUID id, RecurringTransactionUpdateRequest request) {
        UUID tenantId = currentTenantId();
        RecurringTransaction recurring = recurringTransactionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Recurring transaction not found"));
        assertCanModify(recurring);

        Category category = categoryRepository
                .findByIdAndTenantId(request.categoryId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        recurring.setCategory(category);
        recurring.setAmount(request.amount());
        recurring.setDescription(request.description());
        recurring.setType(request.type());
        recurring.setFrequency(request.frequency());
        recurring.setNextRunDate(request.nextRunDate());
        recurring.setActive(request.active());

        return toResponse(recurringTransactionRepository.save(recurring));
    }

    @Transactional
    public void delete(UUID id) {
        RecurringTransaction recurring = recurringTransactionRepository.findByIdAndTenantId(id, currentTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Recurring transaction not found"));
        assertCanModify(recurring);
        recurringTransactionRepository.delete(recurring);
    }

    /**
     * Called by RecurringTransactionScheduler - runs OUTSIDE any HTTP
     * request, so there's no JWT and the Hibernate tenant filter is never
     * enabled here. That's intentional: findByActiveTrueAndNextRunDateLessThanEqual
     * needs to see due rows across every tenant in a single query.
     *
     * Because there's no TenantContext on this thread, tenantId must be set
     * explicitly on every Transaction created below - the @PrePersist
     * fallback in BaseTenantEntity has nothing to fall back to here.
     */
    @Transactional
    public void runDueRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> due = recurringTransactionRepository
                .findByActiveTrueAndNextRunDateLessThanEqual(today);

        for (RecurringTransaction recurring : due) {
            Transaction transaction = new Transaction();
            transaction.setTenantId(recurring.getTenantId()); // explicit - see note above
            transaction.setCategory(recurring.getCategory());
            transaction.setAmount(recurring.getAmount());
            transaction.setDescription(recurring.getDescription());
            transaction.setType(recurring.getType());
            transaction.setTransactionDate(recurring.getNextRunDate());
            transaction.setCreatedBy(recurring.getCreatedBy());
            transactionRepository.save(transaction);

            recurring.setNextRunDate(nextRunDateAfter(recurring));
        }

        log.info("Processed {} due recurring transactions", due.size());
    }

    private LocalDate nextRunDateAfter(RecurringTransaction recurring) {
        return switch (recurring.getFrequency()) {
            case DAILY -> recurring.getNextRunDate().plusDays(1);
            case WEEKLY -> recurring.getNextRunDate().plusWeeks(1);
            case MONTHLY -> recurring.getNextRunDate().plusMonths(1);
        };
    }

    private UUID currentTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Missing tenant context");
        }
        return tenantId;
    }

    private User currentUser() {
        return (User) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void assertCanModify(RecurringTransaction recurring) {
        User actor = currentUser();
        if (actor.getRole() == Role.ADMIN) {
            return;
        }
        if (recurring.getCreatedBy() == null || !recurring.getCreatedBy().getId().equals(actor.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "You can only modify your own recurring transactions"
            );
        }
    }
}
