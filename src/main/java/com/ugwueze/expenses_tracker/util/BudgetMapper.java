package com.ugwueze.expenses_tracker.util;

import com.ugwueze.expenses_tracker.dto.BudgetDto;
import com.ugwueze.expenses_tracker.entity.Budget;
import com.ugwueze.expenses_tracker.entity.User;
import org.springframework.stereotype.Component;

@Component
public final class BudgetMapper {

    private BudgetMapper() {}

    public BudgetDto toDto(Budget budget) {
        if (budget == null) return null;
        return BudgetDto.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .amount(budget.getAmount())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .userId(budget.getUser() != null ? budget.getUser().getId() : null)
                .build();
    }

    public static Budget toEntity(BudgetDto dto, User user) {
        if (dto == null) return null;
        return Budget.builder()
                .id(dto.getId())
                .category(dto.getCategory())
                .amount(dto.getAmount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .user(user)
                .build();
    }

    public static void updateEntity(BudgetDto dto, Budget entity, User user) {
        if (dto == null || entity == null) return;
        entity.setCategory(dto.getCategory());
        entity.setAmount(dto.getAmount());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        if (user != null) {
            entity.setUser(user);
        }
    }
}
