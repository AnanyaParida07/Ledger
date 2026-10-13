package com.budgettracker.controller;

import com.budgettracker.dto.CategorySpendResponse;
import com.budgettracker.dto.MonthlyTrendResponse;
import com.budgettracker.dto.SummaryResponse;
import com.budgettracker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/spend-by-category")
    public List<CategorySpendResponse> spendByCategory(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return analyticsService.spendByCategory(month != null ? month : YearMonth.now());
    }

    @GetMapping("/trend")
    public List<MonthlyTrendResponse> trend(@RequestParam(defaultValue = "6") int months) {
        return analyticsService.monthlyTrend(months);
    }

    @GetMapping("/summary")
    public SummaryResponse summary(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return analyticsService.summary(month != null ? month : YearMonth.now());
    }
}
