package com.ugwueze.expenses_tracker.repository;

import com.ugwueze.expenses_tracker.entity.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {

    @Query(value = "SELECT * FROM recurring_expenses " +
            "WHERE next_occurrence_date <= :date " +
            "AND (end_date IS NULL OR end_date >= :date)",
            nativeQuery = true)
    List<RecurringExpense> findDueByDate(@Param("date") LocalDate date);
}