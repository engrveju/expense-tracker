package com.ugwueze.expenses_tracker.repository;

import com.ugwueze.expenses_tracker.AbstractIntegrationTest;
import com.ugwueze.expenses_tracker.entity.Expense;
import com.ugwueze.expenses_tracker.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ExpenseRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
        testUser = userRepository.save(testUser);

        testExpense = Expense.builder()
                .description("Test Expense")
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .category("Test")
                .paymentMethod(Expense.PaymentMethod.CASH)
                .user(testUser)
                .build();
        testExpense = expenseRepository.save(testExpense);
    }

    @Test
    void findByUserId_ShouldReturnUserExpenses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Expense> result = expenseRepository.findByUserId(testUser.getId(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Expense", result.getContent().get(0).getDescription());
    }

    @Test
    void findByUserIdAndDateBetween_ShouldReturnFilteredExpenses() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        List<Expense> result = expenseRepository.findByUserIdAndDateBetween(
                testUser.getId(), startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Expense", result.get(0).getDescription());
    }

    @Test
    void findByUserIdAndCategory_ShouldReturnCategoryExpenses() {
        List<Expense> result = expenseRepository.findByUserIdAndCategory(
                testUser.getId(), "Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Expense", result.get(0).getDescription());
    }

    @Test
    void getCategoryWiseExpenses_ShouldReturnCategoryTotals() {

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(1);

        Expense anotherExpense = Expense.builder()
                .description("Another Test Expense")
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.now())
                .category("Test")
                .paymentMethod(Expense.PaymentMethod.CREDIT_CARD)
                .user(testUser)
                .build();
        expenseRepository.save(anotherExpense);

        List<Object[]> result = expenseRepository.getCategoryWiseExpenses(
                testUser.getId(), startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0)[0]);
        assertEquals(new BigDecimal("150.00"), result.get(0)[1]);
    }

    @Test
    void existsByIdAndUserId_ShouldReturnTrueForValidExpense() {
        boolean exists = expenseRepository.existsByIdAndUserId(
                testExpense.getId(), testUser.getId());

        assertTrue(exists);
    }

    @Test
    void existsByIdAndUserId_ShouldReturnFalseForInvalidExpense() {
        boolean exists = expenseRepository.existsByIdAndUserId(999L, testUser.getId());
        assertFalse(exists);
    }
}