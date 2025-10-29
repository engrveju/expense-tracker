package com.ugwueze.expenses_tracker.service.impl;

import com.ugwueze.expenses_tracker.dto.BudgetDto;
import com.ugwueze.expenses_tracker.entity.Budget;
import com.ugwueze.expenses_tracker.entity.User;
import com.ugwueze.expenses_tracker.exception.ResourceNotFoundException;
import com.ugwueze.expenses_tracker.repository.BudgetRepository;
import com.ugwueze.expenses_tracker.repository.UserRepository;
import com.ugwueze.expenses_tracker.service.BudgetService;
import com.ugwueze.expenses_tracker.service.ExpenseService;
import com.ugwueze.expenses_tracker.util.BudgetMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final ExpenseService expenseService;

    private final BudgetMapper budgetMapper;

    public BudgetServiceImpl(BudgetRepository budgetRepository,
                             UserRepository userRepository,
                             ExpenseService expenseService,
                             BudgetMapper budgetMapper) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.expenseService = expenseService;
        this.budgetMapper = budgetMapper;
    }

    @Override
    public BudgetDto createBudget(BudgetDto budgetDto) {
        if (budgetDto == null || budgetDto.getUserId() == null) {
            throw new IllegalArgumentException("budget and userId are required");
        }
        User user = userRepository.findById(budgetDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + budgetDto.getUserId()));

        Budget entity = BudgetMapper.toEntity(budgetDto, user);
        Budget saved = budgetRepository.save(entity);
        return budgetMapper.toDto(saved);
    }

    @Override
    public BudgetDto updateBudget(Long id, BudgetDto budgetDto) {
        if (id == null || budgetDto == null || budgetDto.getUserId() == null) {
            throw new IllegalArgumentException("id, budgetDto and userId are required");
        }
        Budget existing = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        if (!Objects.equals(existing.getUser().getId(), budgetDto.getUserId())) {
            throw new ResourceNotFoundException("Budget not found for user: " + budgetDto.getUserId());
        }

        User user = userRepository.findById(budgetDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + budgetDto.getUserId()));

        BudgetMapper.updateEntity(budgetDto, existing, user);
        Budget saved = budgetRepository.save(existing);
        return budgetMapper.toDto(saved);
    }

    @Override
    public void deleteBudget(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("id and userId are required");
        }
        Budget existing = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        if (!Objects.equals(existing.getUser().getId(), userId)) {
            throw new ResourceNotFoundException("Budget not found for user: " + userId);
        }
        budgetRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetDto getBudgetById(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("id and userId are required");
        }
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        if (!Objects.equals(budget.getUser().getId(), userId)) {
            throw new ResourceNotFoundException("Budget not found for user: " + userId);
        }
        return budgetMapper.toDto(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetDto> getUserBudgets(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        return budgets.stream().map(budgetMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetDto getCurrentBudgetForCategory(Long userId, String category) {
        if (userId == null || category == null) {
            throw new IllegalArgumentException("userId and category are required");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        LocalDate today = LocalDate.now();
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        return budgets.stream()
                .filter(b -> category.equalsIgnoreCase(b.getCategory()))
                .filter(b -> !b.getStartDate().isAfter(today) && !b.getEndDate().isBefore(today))
                .findFirst()
                .map(budgetMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBudgetExceeded(Long userId, String category) {
        if (userId == null || category == null) {
            throw new IllegalArgumentException("userId and category are required");
        }
        BudgetDto current = getCurrentBudgetForCategory(userId, category);
        if (current == null) {
            return false;
        }
        LocalDate start = current.getStartDate();
        LocalDate end = current.getEndDate();
        BigDecimal spent = expenseService.getTotalExpensesByDateRange(userId, start, end);
        BigDecimal budgetAmount = current.getAmount() != null ? current.getAmount() : BigDecimal.ZERO;
        return spent.compareTo(budgetAmount) > 0;
    }
}