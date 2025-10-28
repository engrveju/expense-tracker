package com.ugwueze.expenses_tracker.dto;

import com.ugwueze.expenses_tracker.entity.Expense;
import com.ugwueze.expenses_tracker.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class ExpenseDto {
    private Long id;

    @NotBlank(message = "Description is required")
    @Size(max = 255)
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotBlank(message = "Category is required")
    private String category;

    private PaymentMethod paymentMethod;

    private String notes;

    private Long userId;

    public ExpenseDto() {
    }

    public ExpenseDto(Long id,
                      String description,
                      BigDecimal amount,
                      LocalDate date,
                      String category,
                      PaymentMethod paymentMethod,
                      String notes,
                      Long userId) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.userId = userId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

        ExpenseDto that = (ExpenseDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(description, that.description) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(date, that.date) &&
                Objects.equals(category, that.category) &&
                paymentMethod == that.paymentMethod &&
                Objects.equals(notes, that.notes) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, amount, date, category, paymentMethod, notes, userId);
    }

    @Override
    public String toString() {
        return "ExpenseDto{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", category='" + category + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", notes='" + notes + '\'' +
                ", userId=" + userId +
                '}';
    }

    public static final class Builder {
        private Long id;
        private String description;
        private BigDecimal amount;
        private LocalDate date;
        private String category;
        private PaymentMethod paymentMethod;
        private String notes;
        private Long userId;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public ExpenseDto build() {
            return new ExpenseDto(id, description, amount, date, category, paymentMethod, notes, userId);
        }
    }
}
