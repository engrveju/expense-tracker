package com.ugwueze.expenses_tracker.service.impl;

import com.ugwueze.expenses_tracker.entity.Expense;
import com.ugwueze.expenses_tracker.entity.User;
import com.ugwueze.expenses_tracker.exception.ResourceNotFoundException;
import com.ugwueze.expenses_tracker.repository.ExpenseRepository;
import com.ugwueze.expenses_tracker.repository.UserRepository;
import com.ugwueze.expenses_tracker.service.ExpenseExportService;
import com.ugwueze.expenses_tracker.util.CsvUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExpenseExportServiceImpl implements ExpenseExportService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    public ExpenseExportServiceImpl(ExpenseRepository expenseRepository,
                                    UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void streamExpensesAsCsv(Long userId, LocalDate startDate, LocalDate endDate, Writer writer) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("UserNot Found"));


        List<Expense> expenses;
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("date").ascending());

        if (startDate != null && endDate != null) {
            expenses = expenseRepository
                    .findByUserIdAndDateBetween(userId, startDate, endDate);
        } else if (startDate != null) {
            expenses = expenseRepository
                    .findByUserIdAndDateGreaterThanEqual(userId, startDate, pageable)
                    .getContent();
        } else if (endDate != null) {
            expenses = expenseRepository
                    .findByUserIdAndDateLessThanEqual(userId, endDate, pageable)
                    .getContent();
        } else {
            expenses = expenseRepository
                    .findByUserId(userId, pageable)
                    .getContent();
        }

        writer.write(CsvUtils.joinCsvRow("id", "date", "description", "category", "payment_method", "amount", "notes", "created_at", "updated_at"));
        writer.write("\n");

        for (Expense e : expenses) {
            String id = e.getId() == null ? "" : String.valueOf(e.getId());
            String date = e.getDate() == null ? "" : e.getDate().format(DATE_FORMATTER);
            String description = e.getDescription();
            String category = e.getCategory();
            String paymentMethod = e.getPaymentMethod() == null ? "" : e.getPaymentMethod().name();
            String amount = formatAmount(e.getAmount());
            String notes = e.getNotes();
            String created = e.getCreatedAt() == null ? "" : e.getCreatedAt().toString();
            String updated = e.getUpdatedAt() == null ? "" : e.getUpdatedAt().toString();

            writer.write(CsvUtils.joinCsvRow(id, date, description, category, paymentMethod, amount, notes, created, updated));
            writer.write("\n");
        }

        writer.flush();
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "";
        return amount.stripTrailingZeros().toPlainString();
    }
}
