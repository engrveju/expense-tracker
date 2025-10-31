package com.ugwueze.expenses_tracker.controller;

import com.ugwueze.expenses_tracker.dto.ApiResponse;
import com.ugwueze.expenses_tracker.dto.ExpenseDto;
import com.ugwueze.expenses_tracker.dto.ExpenseSummaryDto;
import com.ugwueze.expenses_tracker.dto.MonthlySummaryDto;
import com.ugwueze.expenses_tracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseDto>> createExpense(@Valid @RequestBody ExpenseDto expenseDto) {
        ExpenseDto createdExpense = expenseService.createExpense(expenseDto);
        return new ResponseEntity<>(ApiResponse.success("Expense created successfully", createdExpense), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseDto>> updateExpense(
            @PathVariable Long id, @Valid @RequestBody ExpenseDto expenseDto) {
        ExpenseDto updatedExpense = expenseService.updateExpense(id, expenseDto);
        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", updatedExpense));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable Long id, @RequestParam Long userId) {
        expenseService.deleteExpense(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseDto>> getExpenseById(
            @PathVariable Long id, @RequestParam Long userId) {
        ExpenseDto expense = expenseService.getExpenseById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(expense));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<ExpenseDto>>> getUserExpenses(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ExpenseDto> expenses = expenseService.getUserExpenses(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<ApiResponse<List<ExpenseDto>>> getExpensesByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ExpenseDto> expenses = expenseService.getExpensesByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @GetMapping("/user/{userId}/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalExpensesByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        BigDecimal total = expenseService.getTotalExpensesByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @GetMapping("/user/{userId}/category-wise")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getCategoryWiseExpenses(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, BigDecimal> categoryWise = expenseService.getCategoryWiseExpenses(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(categoryWise));
    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<ApiResponse<ExpenseSummaryDto>> getExpenseSummary(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ExpenseSummaryDto summary = expenseService.getExpenseSummary(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/user/{userId}/category/{category}")
    public ResponseEntity<ApiResponse<List<ExpenseDto>>> getExpensesByCategory(
            @PathVariable Long userId, @PathVariable String category) {
        List<ExpenseDto> expenses = expenseService.getExpensesByCategory(userId, category);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlySummaryDto>> getMonthlySummary(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "month") Integer month
    ) {
        if (year == null || month == null || month < 1 || month > 12) {
            return ResponseEntity.badRequest().build();
        }
        List<MonthlySummaryDto> summary = expenseService.getMonthlySummary(year, month);
        return ResponseEntity.ok(summary);
    }
}