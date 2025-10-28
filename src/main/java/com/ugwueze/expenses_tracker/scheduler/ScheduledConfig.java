package com.ugwueze.expenses_tracker.scheduler;

import com.ugwueze.expenses_tracker.service.RecurringExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class ScheduledConfig {
    @Autowired
    private RecurringExpenseService recurringExpenseService;

    @Scheduled(cron = "0 0 0 * * ?")   // run daily at midnight
    public void runRecurringTemplateJob() {
        recurringExpenseService.processDueRecurringExpenses();
    }
}
