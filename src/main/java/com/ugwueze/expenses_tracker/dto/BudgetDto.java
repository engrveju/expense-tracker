package com.ugwueze.expenses_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class BudgetDto {
    private Long id;

    @NotBlank
    private String category;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private Long userId;

    public BudgetDto() {
    }

    public BudgetDto(Long id, String category, BigDecimal amount, LocalDate startDate, LocalDate endDate, Long userId) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BudgetDto budgetDto = (BudgetDto) o;
        return Objects.equals(id, budgetDto.id) &&
                Objects.equals(category, budgetDto.category) &&
                Objects.equals(amount, budgetDto.amount) &&
                Objects.equals(startDate, budgetDto.startDate) &&
                Objects.equals(endDate, budgetDto.endDate) &&
                Objects.equals(userId, budgetDto.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, amount, startDate, endDate, userId);
    }

    @Override
    public String toString() {
        return "BudgetDto{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", userId=" + userId +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String category;
        private BigDecimal amount;
        private LocalDate startDate;
        private LocalDate endDate;
        private Long userId;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BudgetDto build() {
            return new BudgetDto(id, category, amount, startDate, endDate, userId);
        }
    }
}
