package com.budgettracker.controller;

import com.budgettracker.dto.RecurringTransactionRequest;
import com.budgettracker.dto.RecurringTransactionResponse;
import com.budgettracker.dto.RecurringTransactionUpdateRequest;
import com.budgettracker.service.RecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.UUID;

@RestController
@RequestMapping("/api/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @GetMapping
    public List<RecurringTransactionResponse> list() {
        return recurringTransactionService.listAll();
    }

    @PostMapping
    public ResponseEntity<RecurringTransactionResponse> create(@Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recurringTransactionService.create(request));
    }

    @PatchMapping("/{id}/active")
    public void setActive(@PathVariable UUID id, @RequestParam boolean active) {
        recurringTransactionService.setActive(id, active);
    }

    @PutMapping("/{id}")
    public RecurringTransactionResponse update(@PathVariable UUID id, @Valid @RequestBody RecurringTransactionUpdateRequest request) {
        return recurringTransactionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        recurringTransactionService.delete(id);
    }
}
