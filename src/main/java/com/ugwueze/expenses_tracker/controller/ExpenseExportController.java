package com.ugwueze.expenses_tracker.controller;

import com.ugwueze.expenses_tracker.exception.ResourceNotFoundException;
import com.ugwueze.expenses_tracker.service.ExpenseExportService;
import com.ugwueze.expenses_tracker.util.ValidLocalDate;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;


@RestController
@RequestMapping("/api/users/{userId}/expenses")
public class ExpenseExportController {

    private final ExpenseExportService expenseExportService;

    @Autowired
    public ExpenseExportController(ExpenseExportService expenseExportService) {
        this.expenseExportService = expenseExportService;
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public void exportExpensesAsCsv(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ValidLocalDate LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ValidLocalDate LocalDate endDate,
            HttpServletResponse response
    ) throws IOException {
        try {

            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }

            String filename = "expenses_user_" + userId + ".csv";
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            expenseExportService.streamExpensesAsCsv(userId, startDate, endDate, response.getWriter());

        } catch (ResourceNotFoundException rnfe) {
            response.setContentType("application/json");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            String errorJson = String.format(
                    "{\"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    rnfe.getMessage(),
                    Instant.now()
            );
            response.getWriter().write(errorJson);
        } catch (IllegalArgumentException iae) {
            response.setContentType("application/json");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            String errorJson = String.format(
                    "{\"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    iae.getMessage(),
                    Instant.now()
            );
            response.getWriter().write(errorJson);
        } catch (Exception e) {
            response.setContentType("application/json");
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            String errorJson = String.format(
                    "{\"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    e.getMessage(),
                    Instant.now()
            );
            response.getWriter().write(errorJson);
        }
    }
}
