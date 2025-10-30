package com.ugwueze.expenses_tracker.service;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;

public interface ExpenseExportService {
    void streamExpensesAsCsv(Long userId, LocalDate startDate, LocalDate endDate, Writer writer) throws IOException;
}