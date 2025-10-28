package com.ugwueze.expenses_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugwueze.expenses_tracker.AbstractIntegrationTest;
import com.ugwueze.expenses_tracker.dto.ExpenseDto;
import com.ugwueze.expenses_tracker.entity.Expense;
import com.ugwueze.expenses_tracker.entity.User;
import com.ugwueze.expenses_tracker.enums.PaymentMethod;
import com.ugwueze.expenses_tracker.repository.ExpenseRepository;
import com.ugwueze.expenses_tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExpenseControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    private User testUser;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("integrationuser")
                .email("integration@example.com")
                .firstName("Integration")
                .lastName("Test")
                .build();
        testUser = userRepository.save(testUser);

        testExpense = Expense.builder()
                .description("Integration Test Expense")
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .category("Integration")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .user(testUser)
                .build();
        testExpense = expenseRepository.save(testExpense);
    }

    @Test
    void createExpense_ShouldReturnCreated() throws Exception {
        ExpenseDto expenseDto = ExpenseDto.builder()
                .description("New Integration Expense")
                .amount(new BigDecimal("75.50"))
                .date(LocalDate.now())
                .category("Food")
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .userId(testUser.getId())
                .build();

        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.description").value("New Integration Expense"))
                .andExpect(jsonPath("$.data.amount").value(75.50));
    }

    @Test
    void getExpenseById_ShouldReturnExpense() throws Exception {
        mockMvc.perform(get("/api/v1/expenses/{id}", testExpense.getId())
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testExpense.getId()))
                .andExpect(jsonPath("$.data.description").value("Integration Test Expense"));
    }

    @Test
    void getUserExpenses_ShouldReturnPaginatedExpenses() throws Exception {
        mockMvc.perform(get("/api/v1/expenses/user/{userId}", testUser.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].description").value("Integration Test Expense"));
    }

    @Test
    void updateExpense_ShouldReturnUpdatedExpense() throws Exception {
        ExpenseDto updateDto = ExpenseDto.builder()
                .description("Updated Expense")
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.now())
                .category("Updated Category")
                .paymentMethod(PaymentMethod.CASH)
                .userId(testUser.getId())
                .build();

        mockMvc.perform(put("/api/v1/expenses/{id}", testExpense.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.description").value("Updated Expense"))
                .andExpect(jsonPath("$.data.amount").value(150.00));
    }

    @Test
    void deleteExpense_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/expenses/{id}", testExpense.getId())
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Expense deleted successfully"));
    }

    @Test
    void getExpensesByDateRange_ShouldReturnFilteredExpenses() throws Exception {
        mockMvc.perform(get("/api/v1/expenses/user/{userId}/date-range", testUser.getId())
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].description").value("Integration Test Expense"));
    }
}