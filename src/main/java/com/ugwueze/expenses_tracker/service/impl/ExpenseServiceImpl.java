package com.ugwueze.expenses_tracker.service.impl;

import com.ugwueze.expenses_tracker.dto.ExpenseDto;
import com.ugwueze.expenses_tracker.dto.ExpenseSummaryDto;
import com.ugwueze.expenses_tracker.entity.Expense;
import com.ugwueze.expenses_tracker.entity.User;
import com.ugwueze.expenses_tracker.exception.ResourceNotFoundException;
import com.ugwueze.expenses_tracker.repository.ExpenseRepository;
import com.ugwueze.expenses_tracker.repository.UserRepository;
import com.ugwueze.expenses_tracker.service.ExpenseService;
import com.ugwueze.expenses_tracker.util.ExpenseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseMapper expenseMapper;


    public ExpenseServiceImpl() {

    }

    @Override
    public ExpenseDto createExpense(ExpenseDto expenseDto) {

        User user = userRepository.findById(expenseDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + expenseDto.getUserId()));

        Expense expense = expenseMapper.toEntity(expenseDto);
        expense.setUser(user);

        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toDto(savedExpense);
    }

    @Override
    public ExpenseDto updateExpense(Long id, ExpenseDto expenseDto) {

        Expense existingExpense = expenseRepository.findByIdAndUserId(id, expenseDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        expenseMapper.updateEntityFromDto(expenseDto, existingExpense);
        Expense updatedExpense = expenseRepository.save(existingExpense);
        return expenseMapper.toDto(updatedExpense);
    }

    @Override
    public void deleteExpense(Long id, Long userId) {
        if (!expenseRepository.existsByIdAndUserId(id, userId)) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }

        expenseRepository.deleteByIdAndUserId(id, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDto getExpenseById(Long id, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        return expenseMapper.toDto(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseDto> getUserExpenses(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return expenseRepository.findByUserId(userId, pageable)
                .map(expenseMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDto> getExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                .stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.getTotalExpensesByUserAndDateRange(userId, startDate, endDate)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCategoryWiseExpenses(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = expenseRepository.getCategoryWiseExpenses(userId, startDate, endDate);

        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (BigDecimal) result[1]
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseSummaryDto getExpenseSummary(Long userId, LocalDate startDate, LocalDate endDate) {

        BigDecimal totalAmount = getTotalExpensesByDateRange(userId, startDate, endDate);
        List<ExpenseDto> expenses = getExpensesByDateRange(userId, startDate, endDate);
        Map<String, BigDecimal> categoryWise = getCategoryWiseExpenses(userId, startDate, endDate);

        String topCategory = categoryWise.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No expenses");

        BigDecimal topCategoryAmount = categoryWise.getOrDefault(topCategory, BigDecimal.ZERO);

        return ExpenseSummaryDto.builder()
                .period(startDate + " to " + endDate)
                .totalAmount(totalAmount)
                .totalExpenses((long) expenses.size())
                .topCategory(topCategory)
                .topCategoryAmount(topCategoryAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDto> getExpensesByCategory(Long userId, String category) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return expenseRepository.findByUserIdAndCategory(userId, category)
                .stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Integer, BigDecimal> getMonthlyExpensesSummary(Long userId, int year) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (year < 1900 || year > 3000) {
            throw new IllegalArgumentException("year out of range: " + year);
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

        Map<Integer, BigDecimal> monthly = java.util.stream.IntStream.rangeClosed(1, 12)
                .boxed()
                .collect(Collectors.toMap(m -> m, m -> BigDecimal.ZERO, (a, b) -> a, java.util.LinkedHashMap::new));

        for (Expense e : expenses) {
            if (e == null) {
                continue;
            }
            LocalDate d = e.getDate();
            if (d == null) {
                continue;
            }
            int month = d.getMonthValue();
            BigDecimal amount = e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO;
            amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
            monthly.merge(month, amount, BigDecimal::add);
        }

        Map<Integer, BigDecimal> result = monthly.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new
                ));

        return result;
    }

}
