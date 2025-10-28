package com.ugwueze.expenses_tracker.serviceIT;

import com.ugwueze.expenses_tracker.AbstractIntegrationTest;
import com.ugwueze.expenses_tracker.dto.ExpenseDto;
import com.ugwueze.expenses_tracker.entity.Expense;
import com.ugwueze.expenses_tracker.entity.User;
import com.ugwueze.expenses_tracker.enums.PaymentMethod;
import com.ugwueze.expenses_tracker.exception.ResourceNotFoundException;
import com.ugwueze.expenses_tracker.repository.ExpenseRepository;
import com.ugwueze.expenses_tracker.repository.UserRepository;
import com.ugwueze.expenses_tracker.service.ExpenseService;
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
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ExpenseServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

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
                .paymentMethod(PaymentMethod.CASH)
                .user(testUser)
                .build();
        testExpense = expenseRepository.save(testExpense);
    }

    @Test
    void createExpense_ShouldReturnCreatedExpense() {
        ExpenseDto expenseDto = ExpenseDto.builder()
                .description("New Expense")
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.now())
                .category("Food")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .userId(testUser.getId())
                .build();

        ExpenseDto result = expenseService.createExpense(expenseDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("New Expense", result.getDescription());
        assertEquals(new BigDecimal("150.00"), result.getAmount());
        assertEquals(testUser.getId(), result.getUserId());

        // Verify persistence
        Optional<Expense> persistedExpense = expenseRepository.findById(result.getId());
        assertTrue(persistedExpense.isPresent());
        assertEquals("New Expense", persistedExpense.get().getDescription());
    }

    @Test
    void createExpense_WhenUserNotFound_ShouldThrowException() {

        ExpenseDto expenseDto = ExpenseDto.builder()
                .description("New Expense")
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.now())
                .category("Food")
                .userId(999L)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> expenseService.createExpense(expenseDto));
    }

    @Test
    void getExpenseById_ShouldReturnExpense() {
        ExpenseDto result = expenseService.getExpenseById(testExpense.getId(), testUser.getId());
        assertNotNull(result);
        assertEquals(testExpense.getId(), result.getId());
        assertEquals("Test Expense", result.getDescription());
    }

    @Test
    void getExpenseById_WhenNotFound_ShouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
                () -> expenseService.getExpenseById(999L, testUser.getId()));
    }

    @Test
    void deleteExpense_ShouldDeleteSuccessfully() {
        expenseService.deleteExpense(testExpense.getId(), testUser.getId());

        Optional<Expense> deletedExpense = expenseRepository.findById(testExpense.getId());
        assertFalse(deletedExpense.isPresent());
    }

    @Test
    void getUserExpenses_ShouldReturnPageOfExpenses() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ExpenseDto> result = expenseService.getUserExpenses(testUser.getId(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Expense", result.getContent().get(0).getDescription());
    }

    @Test
    void getExpensesByDateRange_ShouldReturnFilteredExpenses() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(1);

        List<ExpenseDto> result = expenseService.getExpensesByDateRange(
                testUser.getId(), startDate, endDate);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Expense", result.get(0).getDescription());
    }

    @Test
    void getTotalExpensesByDateRange_ShouldReturnCorrectTotal() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(1);

        Expense anotherExpense = Expense.builder()
                .description("Another Expense")
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.now())
                .category("Entertainment")
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .user(testUser)
                .build();
        expenseRepository.save(anotherExpense);

        BigDecimal total = expenseService.getTotalExpensesByDateRange(
                testUser.getId(), startDate, endDate);

        assertEquals(new BigDecimal("150.00"), total);
    }

    @Test
    void getCategoryWiseExpenses_ShouldReturnCategoryBreakdown() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(1);

        Expense foodExpense = Expense.builder()
                .description("Food Expense")
                .amount(new BigDecimal("75.00"))
                .date(LocalDate.now())
                .category("Food")
                .paymentMethod(PaymentMethod.CASH)
                .user(testUser)
                .build();
        expenseRepository.save(foodExpense);

        var categoryWise = expenseService.getCategoryWiseExpenses(
                testUser.getId(), startDate, endDate);

        assertNotNull(categoryWise);
        assertEquals(2, categoryWise.size());
        assertEquals(new BigDecimal("100.00"), categoryWise.get("Test"));
        assertEquals(new BigDecimal("75.00"), categoryWise.get("Food"));
    }
}