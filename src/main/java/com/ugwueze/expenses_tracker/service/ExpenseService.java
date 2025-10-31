package com.ugwueze.expenses_tracker.service;

import com.ugwueze.expenses_tracker.dto.ExpenseDto;
import com.ugwueze.expenses_tracker.dto.ExpenseSummaryDto;
import com.ugwueze.expenses_tracker.dto.MonthlySummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ExpenseService {

    ExpenseDto createExpense(ExpenseDto expenseDto);

    ExpenseDto updateExpense(Long id, ExpenseDto expenseDto);

    void deleteExpense(Long id, Long userId);

    ExpenseDto getExpenseById(Long id, Long userId);

    Page<ExpenseDto> getUserExpenses(Long userId, Pageable pageable);

    List<ExpenseDto> getExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    BigDecimal getTotalExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    Map<String, BigDecimal> getCategoryWiseExpenses(Long userId, LocalDate startDate, LocalDate endDate);

    ExpenseSummaryDto getExpenseSummary(Long userId, LocalDate startDate, LocalDate endDate);

    List<ExpenseDto> getExpensesByCategory(Long userId, String category);

    @Transactional(readOnly = true)
    Map<Integer, BigDecimal> getMonthlyExpensesSummary(Long userId, int year);

    @Transactional(readOnly = true)
    List<MonthlySummaryDto> getMonthlySummary(int year, int month);
}