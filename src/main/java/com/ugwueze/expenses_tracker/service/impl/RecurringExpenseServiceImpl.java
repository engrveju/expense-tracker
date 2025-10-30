package com.ugwueze.expenses_tracker.service.impl;


import com.ugwueze.expenses_tracker.entity.Expense;
import com.ugwueze.expenses_tracker.entity.RecurringExpense;
import com.ugwueze.expenses_tracker.entity.User;
import com.ugwueze.expenses_tracker.enums.RecurrenceType;
import com.ugwueze.expenses_tracker.repository.ExpenseRepository;
import com.ugwueze.expenses_tracker.repository.RecurringExpenseRepository;
import com.ugwueze.expenses_tracker.repository.UserRepository;
import com.ugwueze.expenses_tracker.service.RecurringExpenseService;
import com.ugwueze.expenses_tracker.util.RecurrenceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecurringExpenseServiceImpl implements RecurringExpenseService {

    private final RecurringExpenseRepository recurringExpenseRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public RecurringExpenseServiceImpl(RecurringExpenseRepository recurringExpenseRepository, ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.recurringExpenseRepository = recurringExpenseRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public void processDueRecurringExpenses() {
        LocalDate today = LocalDate.now();
        List<RecurringExpense> due = recurringExpenseRepository.findDueByDate(today);
        for (RecurringExpense template : due) {
            processRecurringTemplate(template);
        }
    }


    @Transactional
    public void processRecurringTemplate(RecurringExpense template) {
        if (template == null) {
            return;
        }
        if (!template.isActive()) {
            return;
        }

        LocalDate processingDate = LocalDate.now();
        LocalDate candidate = template.getNextOccurrenceDate();
        LocalDate endDate = template.getEndDate();
        RecurrenceType recurrenceType = template.getRecurrenceType();
        int interval = template.getInterval() == null ? 1 : template.getInterval();

        if (candidate == null) {
            template.setActive(false);
            recurringExpenseRepository.save(template);
            return;
        }

        if (interval <= 0) {
            template.setActive(false);
            recurringExpenseRepository.save(template);
            return;
        }

        RecurrenceType utilsType = convertToUtilsType(recurrenceType);

        if (!RecurrenceUtils.isOccurrenceWithinEndDate(candidate, endDate)) {
            template.setActive(false);
            recurringExpenseRepository.save(template);
            return;
        }

        int createdCount = 0;
        int safetyLimit = 1000;
        int iterations = 0;

        while (candidate != null
                && RecurrenceUtils.isOccurrenceWithinEndDate(candidate, endDate)
                && !candidate.isAfter(processingDate)
                && iterations < safetyLimit) {

            createExpenseFromTemplate(template, candidate);
            createdCount++;

            candidate = RecurrenceUtils.nextOccurrence(candidate, utilsType, interval);
            iterations++;
        }

        if (iterations >= safetyLimit) {

            recurringExpenseRepository.save(template);
            return;
        }

        if (!RecurrenceUtils.isOccurrenceWithinEndDate(candidate, endDate)) {
            template.setActive(false);
        }
        template.setNextOccurrenceDate(candidate);
        recurringExpenseRepository.save(template);
    }

    private void createExpenseFromTemplate(RecurringExpense template, LocalDate occurrenceDate) {
        User user = userRepository.findById(template.getUserId()).orElseThrow();
        Expense expense = new Expense();
        expense.setUser(user);
        expense.setCategory(template.getCategory());
        expense.setAmount(template.getAmount());
        expense.setDescription(template.getDescription());
        expense.setPaymentMethod(template.getPaymentMethod());
        expense.setDate(occurrenceDate);
        expense.setCreatedAt(LocalDateTime.now());

        expenseRepository.save(expense);
    }


    private RecurrenceType convertToUtilsType(RecurrenceType domainType) {
        return switch (domainType) {
            case DAILY -> RecurrenceType.DAILY;
            case WEEKLY -> RecurrenceType.WEEKLY;
            case MONTHLY -> RecurrenceType.MONTHLY;
            case YEARLY -> RecurrenceType.YEARLY;
        };
    }
}
