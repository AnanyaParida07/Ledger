package com.budgettracker.config;

import com.budgettracker.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecurringTransactionScheduler {

    private final RecurringTransactionService recurringTransactionService;

    // Runs once a day at 01:00 server time. Cron format: sec min hour day month weekday.
    @Scheduled(cron = "0 0 1 * * *")
    public void processRecurringTransactions() {
        recurringTransactionService.runDueRecurringTransactions();
    }
}