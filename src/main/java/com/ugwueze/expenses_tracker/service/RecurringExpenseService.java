package com.ugwueze.expenses_tracker.service;

import org.springframework.transaction.annotation.Transactional;

public interface RecurringExpenseService {
    @Transactional
    void processDueRecurringExpenses();
}
