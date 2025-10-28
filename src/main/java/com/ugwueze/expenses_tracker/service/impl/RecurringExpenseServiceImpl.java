package com.ugwueze.expenses_tracker.service.impl;


import com.ugwueze.expenses_tracker.entity.Expense;
import com.ugwueze.expenses_tracker.entity.RecurringExpense;
import com.ugwueze.expenses_tracker.entity.User;
import com.ugwueze.expenses_tracker.repository.ExpenseRepository;
import com.ugwueze.expenses_tracker.repository.RecurringExpenseRepository;
import com.ugwueze.expenses_tracker.repository.UserRepository;
import com.ugwueze.expenses_tracker.service.RecurringExpenseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
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
        List<RecurringExpense> due = recurringExpenseRepository.findDueByDate( today);
        for (RecurringExpense template : due) {
            User user = userRepository.findById(template.getUserId()).orElseThrow();
            Expense expense = new Expense();
            expense.setUser(user);
            expense.setCategory(template.getCategory());
            expense.setPaymentMethod(template.getPaymentMethod());
            expense.setAmount(template.getAmount());
            expense.setDate(template.getNextOccurrenceDate());
            expense.setDescription(template.getDescription());

            expenseRepository.save(expense);
            template.advanceNextOccurrence();
            if (template.getEndDate() != null && !template.getNextOccurrenceDate().isBefore(template.getEndDate().plusDays(1))) {
                recurringExpenseRepository.delete(template);
            } else {
                recurringExpenseRepository.save(template);
            }
        }
    }
}
