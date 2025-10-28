package com.ugwueze.expenses_tracker.util;

import com.ugwueze.expenses_tracker.dto.ExpenseDto;
import com.ugwueze.expenses_tracker.entity.Expense;
import com.ugwueze.expenses_tracker.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {


    public ExpenseDto toDto(Expense expense) {
        if (expense == null) {
            return null;
        }

        Long userId = expense.getUser() != null ? expense.getUser().getId() : null;

        return ExpenseDto.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .category(expense.getCategory())
                .paymentMethod(expense.getPaymentMethod())
                .notes(expense.getNotes())
                .userId(userId)
                .build();
    }


    public Expense toEntity(ExpenseDto expenseDto) {
        if (expenseDto == null) {
            return null;
        }

        Expense expense = new Expense();
        expense.setId(expenseDto.getId());
        expense.setDescription(expenseDto.getDescription());
        expense.setAmount(expenseDto.getAmount());
        expense.setDate(expenseDto.getDate());
        expense.setCategory(expenseDto.getCategory());
        expense.setPaymentMethod(expenseDto.getPaymentMethod());
        expense.setNotes(expenseDto.getNotes());

        if (expenseDto.getUserId() != null) {
            User user = new User();
            user.setId(expenseDto.getUserId());
            expense.setUser(user);
        }

        return expense;
    }


    public void updateEntityFromDto(ExpenseDto expenseDto, Expense expense) {
        if (expenseDto == null || expense == null) {
            return;
        }

        if (expenseDto.getDescription() != null) {
            expense.setDescription(expenseDto.getDescription());
        }
        if (expenseDto.getAmount() != null) {
            expense.setAmount(expenseDto.getAmount());
        }
        if (expenseDto.getDate() != null) {
            expense.setDate(expenseDto.getDate());
        }
        if (expenseDto.getCategory() != null) {
            expense.setCategory(expenseDto.getCategory());
        }
        if (expenseDto.getPaymentMethod() != null) {
            expense.setPaymentMethod(expenseDto.getPaymentMethod());
        }
        if (expenseDto.getNotes() != null) {
            expense.setNotes(expenseDto.getNotes());
        }

        if (expenseDto.getUserId() != null) {
            User user = new User();
            user.setId(expenseDto.getUserId());
            expense.setUser(user);
        }
    }
}
