package com.ugwueze.expenses_tracker.controller;

import com.ugwueze.expenses_tracker.dto.ApiResponse;
import com.ugwueze.expenses_tracker.dto.BudgetDto;
import com.ugwueze.expenses_tracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetDto>> createBudget(@Valid @RequestBody BudgetDto budgetDto) {
        BudgetDto created = budgetService.createBudget(budgetDto);
        return new ResponseEntity<>(ApiResponse.success("Budget created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetDto>> updateBudget(
            @PathVariable Long id, @Valid @RequestBody BudgetDto budgetDto) {
        BudgetDto updated = budgetService.updateBudget(id, budgetDto);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @PathVariable Long id, @RequestParam Long userId) {
        budgetService.deleteBudget(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetDto>> getBudgetById(
            @PathVariable Long id, @RequestParam Long userId) {
        BudgetDto dto = budgetService.getBudgetById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BudgetDto>>> getUserBudgets(@PathVariable Long userId) {
        List<BudgetDto> budgets = budgetService.getUserBudgets(userId);
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    @GetMapping("/user/{userId}/current")
    public ResponseEntity<ApiResponse<BudgetDto>> getCurrentBudgetForCategory(
            @PathVariable Long userId, @RequestParam String category) {
        BudgetDto dto = budgetService.getCurrentBudgetForCategory(userId, category);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/user/{userId}/category/{category}/exceeded")
    public ResponseEntity<ApiResponse<Boolean>> isBudgetExceeded(
            @PathVariable Long userId, @PathVariable String category) {
        boolean exceeded = budgetService.isBudgetExceeded(userId, category);
        return ResponseEntity.ok(ApiResponse.success(exceeded));
    }

}
