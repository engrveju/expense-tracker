package com.ugwueze.expenses_tracker.controller;

import com.ugwueze.expenses_tracker.AbstractIntegrationTest;
import com.ugwueze.expenses_tracker.entity.RecurringExpense;
import com.ugwueze.expenses_tracker.entity.User;
import com.ugwueze.expenses_tracker.enums.PaymentMethod;
import com.ugwueze.expenses_tracker.enums.RecurrenceType;
import com.ugwueze.expenses_tracker.repository.RecurringExpenseRepository;
import com.ugwueze.expenses_tracker.repository.UserRepository;
import com.ugwueze.expenses_tracker.service.RecurringExpenseService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecurringExpenseControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecurringExpenseRepository repo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecurringExpenseService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRecurringExpense_returnsSaved() throws Exception {
        RecurringExpense template = new RecurringExpense();
        template.setUserId(300L);
        template.setCategory("Gym");
        template.setPaymentMethod(PaymentMethod.CASH);
        template.setAmount(BigDecimal.valueOf(30.0));
        template.setRecurrenceType(RecurrenceType.WEEKLY);
        template.setNextOccurrenceDate(LocalDate.now());

        String payload = objectMapper.writeValueAsString(template);

        mockMvc.perform(post("/api/expenses/recurring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Gym"))
                .andExpect(jsonPath("$.recurrenceType").value("WEEKLY"));

        List<RecurringExpense> all = repo.findAll();
        assertFalse(all.isEmpty(), "Repository should contain the saved recurring expense");
        RecurringExpense saved = all.stream()
                .filter(r -> "Gym".equals(r.getCategory()) && Long.valueOf(300L).equals(r.getUserId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Saved recurring expense not found"));
        assertNotNull(saved.getId());
        assertEquals(RecurrenceType.WEEKLY, saved.getRecurrenceType());
    }

    @Test
    void triggerProcess_advancesNextOccurrenceDate_forDueExpense() throws Exception {
        User user = User.builder()
                .username("testuser2")
                .email("teeust@example.com")
                .firstName("Test2")
                .lastName("User2")
                .build();
        userRepository.save(user);

        RecurringExpense due = new RecurringExpense();
        due.setUserId(user.getId());
        due.setCategory("Subscription");
        due.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        due.setAmount(BigDecimal.valueOf(10.0));
        due.setRecurrenceType(RecurrenceType.WEEKLY);
        due.setDescription("Weekly Subscription");
        LocalDate oldDate = LocalDate.now().minusDays(1);
        due.setNextOccurrenceDate(oldDate);
        due = repo.save(due);

        mockMvc.perform(post("/api/expenses/recurring/process"))
                .andExpect(status().isOk());

        RecurringExpense updated = repo.findById(due.getId()).orElseThrow();
        assertTrue(updated.getNextOccurrenceDate().isAfter(oldDate),
                "Next occurrence date should be advanced after processing");
    }

}
