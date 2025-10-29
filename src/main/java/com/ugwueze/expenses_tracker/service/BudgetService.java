package com.ugwueze.expenses_tracker.service;

import com.ugwueze.expenses_tracker.dto.BudgetDto;
import java.util.List;

public interface BudgetService {

    BudgetDto createBudget(BudgetDto budgetDto);

    BudgetDto updateBudget(Long id, BudgetDto budgetDto);

    void deleteBudget(Long id, Long userId);

    BudgetDto getBudgetById(Long id, Long userId);

    List<BudgetDto> getUserBudgets(Long userId);

    BudgetDto getCurrentBudgetForCategory(Long userId, String category);

    boolean isBudgetExceeded(Long userId, String category);

}