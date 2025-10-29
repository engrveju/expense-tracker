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


    public void processRecurringTemplate(RecurringExpense template) {
        LocalDate candidate = template.getNextOccurrenceDate();
        LocalDate endDate = template.getEndDate();
        RecurrenceType type = convertToUtilsType(template.getRecurrenceType());
        int interval = template.getInterval() == null ? 1 : template.getInterval();

        if (RecurrenceUtils.isOccurrenceWithinEndDate(candidate, endDate)) {
            createExpenseFromTemplate(template, candidate);

            LocalDate newNext = RecurrenceUtils.nextOccurrence(candidate, type, interval);
            if (!RecurrenceUtils.isOccurrenceWithinEndDate(newNext, endDate)) {
                template.setActive(false);
                recurringExpenseRepository.save(template);
            } else {
                template.setNextOccurrenceDate(newNext);
                recurringExpenseRepository.save(template);
            }
        } else {
            template.setActive(false);
            recurringExpenseRepository.save(template);
        }
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
