package com.ugwueze.expenses_tracker.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class ExpenseSummaryDto {
    private String period;
    private BigDecimal totalAmount;
    private Long totalExpenses;
    private String topCategory;
    private BigDecimal topCategoryAmount;

    public ExpenseSummaryDto() {
    }

    public ExpenseSummaryDto(String period,
                             BigDecimal totalAmount,
                             Long totalExpenses,
                             String topCategory,
                             BigDecimal topCategoryAmount) {
        this.period = period;
        this.totalAmount = totalAmount;
        this.totalExpenses = totalExpenses;
        this.topCategory = topCategory;
        this.topCategoryAmount = topCategoryAmount;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(Long totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public String getTopCategory() {
        return topCategory;
    }

    public void setTopCategory(String topCategory) {
        this.topCategory = topCategory;
    }

    public BigDecimal getTopCategoryAmount() {
        return topCategoryAmount;
    }

    public void setTopCategoryAmount(BigDecimal topCategoryAmount) {
        this.topCategoryAmount = topCategoryAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpenseSummaryDto that = (ExpenseSummaryDto) o;
        return Objects.equals(period, that.period) &&
                Objects.equals(totalAmount, that.totalAmount) &&
                Objects.equals(totalExpenses, that.totalExpenses) &&
                Objects.equals(topCategory, that.topCategory) &&
                Objects.equals(topCategoryAmount, that.topCategoryAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(period, totalAmount, totalExpenses, topCategory, topCategoryAmount);
    }

    @Override
    public String toString() {
        return "ExpenseSummaryDto{" +
                "period='" + period + '\'' +
                ", totalAmount=" + totalAmount +
                ", totalExpenses=" + totalExpenses +
                ", topCategory='" + topCategory + '\'' +
                ", topCategoryAmount=" + topCategoryAmount +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String period;
        private BigDecimal totalAmount;
        private Long totalExpenses;
        private String topCategory;
        private BigDecimal topCategoryAmount;

        private Builder() {
        }

        public Builder period(String period) {
            this.period = period;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder totalExpenses(Long totalExpenses) {
            this.totalExpenses = totalExpenses;
            return this;
        }

        public Builder topCategory(String topCategory) {
            this.topCategory = topCategory;
            return this;
        }

        public Builder topCategoryAmount(BigDecimal topCategoryAmount) {
            this.topCategoryAmount = topCategoryAmount;
            return this;
        }

        public ExpenseSummaryDto build() {
            return new ExpenseSummaryDto(period, totalAmount, totalExpenses, topCategory, topCategoryAmount);
        }
    }
}
