package com.budgettracker.controller;

import com.budgettracker.dto.PagedResponse;
import com.budgettracker.dto.TransactionRequest;
import com.budgettracker.dto.TransactionResponse;
import com.budgettracker.dto.TransactionUpdateRequest;
import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.entity.User;
import com.budgettracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public List<TransactionResponse> list(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return transactionService.listForMonth(
                month != null ? month : YearMonth.now()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TransactionResponse>> search(
            Authentication authentication,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {

        Sort sort;

        if ("transactionDate".equals(sortBy)) {
            sort = Sort.by(
                    Sort.Direction.fromString(direction),
                    "transactionDate"
            );
        } else {
            sort = Sort.by(
                    Sort.Direction.fromString(direction),
                    sortBy
            );
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Transaction> transactions = transactionService.search(
                getCurrentTenantId(authentication),
                startDate,
                endDate,
                categoryId,
                type,
                minAmount,
                maxAmount,
                pageable
        );

        return ResponseEntity.ok(
                transactions.map(TransactionResponse::fromEntity)
        );
    }

    @PostMapping
    public TransactionResponse create(
            @Valid @RequestBody TransactionRequest request
    ) {
        return transactionService.create(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        transactionService.delete(id);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody TransactionUpdateRequest request
    ) {
        return transactionService.update(id, request);
    }

    private UUID getCurrentTenantId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getTenantId();
    }
}
