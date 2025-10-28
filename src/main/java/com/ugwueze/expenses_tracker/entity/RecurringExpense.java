package com.ugwueze.expenses_tracker.entity;

import com.ugwueze.expenses_tracker.enums.PaymentMethod;
import com.ugwueze.expenses_tracker.enums.RecurrenceType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_expenses")
public class RecurringExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String category;

    private PaymentMethod paymentMethod;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private RecurrenceType recurrenceType;

    private LocalDate nextOccurrenceDate;

    private LocalDate endDate;

    private String description;

    public RecurringExpense() {

    }

    public void advanceNextOccurrence() {
        if (recurrenceType == RecurrenceType.WEEKLY) {
            nextOccurrenceDate = nextOccurrenceDate.plusWeeks(1);
        } else if (recurrenceType == RecurrenceType.MONTHLY) {
            nextOccurrenceDate = nextOccurrenceDate.plusMonths(1);
        } else if (recurrenceType == RecurrenceType.YEARLY) {
            nextOccurrenceDate = nextOccurrenceDate.plusYears(1);
        }
    }

    public RecurringExpense(Long id, Long userId, String category, PaymentMethod paymentMethod, BigDecimal amount, RecurrenceType recurrenceType, LocalDate nextOccurrenceDate, LocalDate endDate) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.recurrenceType = recurrenceType;
        this.nextOccurrenceDate = nextOccurrenceDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public LocalDate getNextOccurrenceDate() {
        return nextOccurrenceDate;
    }

    public void setNextOccurrenceDate(LocalDate nextOccurrenceDate) {
        this.nextOccurrenceDate = nextOccurrenceDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
