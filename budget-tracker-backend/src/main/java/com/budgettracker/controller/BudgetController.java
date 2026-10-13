package com.budgettracker.controller;

import com.budgettracker.dto.BudgetRequest;
import com.budgettracker.dto.BudgetStatusResponse;
import com.budgettracker.dto.BudgetUpdateRequest;
import com.budgettracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public List<BudgetStatusResponse> status(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return budgetService.statusForMonth(month != null ? month : YearMonth.now());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BudgetStatusResponse setBudget(@Valid @RequestBody BudgetRequest request) {
        return budgetService.setBudget(request);
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public BudgetStatusResponse updateBudget(
            @PathVariable UUID categoryId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @Valid @RequestBody BudgetUpdateRequest request
    ) {
        return budgetService.updateBudget(categoryId, month, request);
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBudget(
            @PathVariable UUID categoryId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        budgetService.deleteBudget(categoryId, month);
    }
}
