package com.ugwueze.expenses_tracker.service;

import com.ugwueze.expenses_tracker.dto.ExpenseDto;
import com.ugwueze.expenses_tracker.entity.Expense;
import com.ugwueze.expenses_tracker.entity.User;
import com.ugwueze.expenses_tracker.enums.PaymentMethod;
import com.ugwueze.expenses_tracker.exception.ResourceNotFoundException;
import com.ugwueze.expenses_tracker.repository.ExpenseRepository;
import com.ugwueze.expenses_tracker.repository.UserRepository;
import com.ugwueze.expenses_tracker.service.impl.ExpenseServiceImpl;
import com.ugwueze.expenses_tracker.util.ExpenseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceUnitTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private User testUser;
    private Expense testExpense;
    private ExpenseDto testExpenseDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        testExpense = Expense.builder()
                .id(1L)
                .description("Test Expense")
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .category("Test")
                .paymentMethod(PaymentMethod.CASH)
                .user(testUser)
                .build();

        testExpenseDto = ExpenseDto.builder()
                .id(1L)
                .description("Test Expense")
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .category("Test")
                .paymentMethod(PaymentMethod.CASH)
                .userId(1L)
                .build();
    }

    @Test
    void createExpense_ShouldReturnCreatedExpense() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(expenseMapper.toEntity(testExpenseDto)).thenReturn(testExpense);
        when(expenseRepository.save(testExpense)).thenReturn(testExpense);
        when(expenseMapper.toDto(testExpense)).thenReturn(testExpenseDto);

        ExpenseDto result = expenseService.createExpense(testExpenseDto);

        assertNotNull(result);
        assertEquals(testExpenseDto.getDescription(), result.getDescription());
        verify(expenseRepository).save(testExpense);
    }

    @Test
    void getExpenseById_WhenNotFound_ShouldThrowException() {
        when(expenseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.getExpenseById(1L, 1L));
    }

    @Test
    void getUserExpenses_ShouldReturnPageOfExpenses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Expense> expensePage = new PageImpl<>(List.of(testExpense));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(expenseRepository.findByUserId(1L, pageable)).thenReturn(expensePage);
        when(expenseMapper.toDto(testExpense)).thenReturn(testExpenseDto);

        Page<ExpenseDto> result = expenseService.getUserExpenses(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(expenseRepository).findByUserId(1L, pageable);
    }
}