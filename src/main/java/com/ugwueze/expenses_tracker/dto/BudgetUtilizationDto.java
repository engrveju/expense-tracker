package com.ugwueze.expenses_tracker.dto;

import java.math.BigDecimal;

public class BudgetUtilizationDto {

    private Long budgetId;
    private String category;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private BigDecimal percentUsed;

    public BudgetUtilizationDto() {}

    private BudgetUtilizationDto(Builder b) {
        this.budgetId = b.budgetId;
        this.category = b.category;
        this.budgetAmount = b.budgetAmount;
        this.spentAmount = b.spentAmount;
        this.remainingAmount = b.remainingAmount;
        this.percentUsed = b.percentUsed;
    }

    public static Builder builder() { return new Builder(); }

    public Long getBudgetId() { return budgetId; }
    public String getCategory() { return category; }
    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public BigDecimal getSpentAmount() { return spentAmount; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public BigDecimal getPercentUsed() { return percentUsed; }

    public static class Builder {
        private Long budgetId;
        private String category;
        private BigDecimal budgetAmount;
        private BigDecimal spentAmount;
        private BigDecimal remainingAmount;
        private BigDecimal percentUsed;

        public Builder budgetId(Long id) { this.budgetId = id; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder budgetAmount(BigDecimal amount) { this.budgetAmount = amount; return this; }
        public Builder spentAmount(BigDecimal spent) { this.spentAmount = spent; return this; }
        public Builder remainingAmount(BigDecimal rem) { this.remainingAmount = rem; return this; }
        public Builder percentUsed(BigDecimal pct) { this.percentUsed = pct; return this; }
        public BudgetUtilizationDto build() { return new BudgetUtilizationDto(this); }
    }
}
