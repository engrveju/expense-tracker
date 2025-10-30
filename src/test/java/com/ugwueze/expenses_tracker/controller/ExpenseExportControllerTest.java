package com.ugwueze.expenses_tracker.controller;

import com.ugwueze.expenses_tracker.exception.ResourceNotFoundException;
import com.ugwueze.expenses_tracker.service.ExpenseExportService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.io.Writer;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ExpenseExportController.class)
class ExpenseExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseExportService expenseExportService;

    @Test
    void exportExpensesAsCsv_withoutFilters_streamsCsvAndSetsHeaders() throws Exception {
        doAnswer(invocation -> {
            Writer w = invocation.getArgument(3);
            w.write("id,date\n1,2023-01-02");
            w.flush();
            return null;
        }).when(expenseExportService).streamExpensesAsCsv(anyLong(), isNull(), isNull(), any(Writer.class));


        mockMvc.perform(get("/api/users/{userId}/expenses/export", 5L))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"expenses_user_5.csv\""))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id,date")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1,2023-01-02")));

        Mockito.verify(expenseExportService).streamExpensesAsCsv(eq(5L), isNull(), isNull(), any(Writer.class));
    }

    @Test
    void exportExpensesAsCsv_withInvalidDate_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/expenses/export", 5L)
                        .param("startDate", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportExpensesAsCsv_whenServiceThrowsNotFound_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("UserNot Found"))
                .when(expenseExportService).streamExpensesAsCsv(eq(99L), any(), any(), any(Writer.class));

        mockMvc.perform(get("/api/users/{userId}/expenses/export", 99L))
                .andExpect(status().isNotFound());
    }
}
