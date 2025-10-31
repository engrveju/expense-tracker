package com.ugwueze.expenses_tracker.service;

import com.ugwueze.expenses_tracker.dto.ExpenseDto;
import com.ugwueze.expenses_tracker.dto.MonthlySummaryDto;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

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

    @Test
    void getMonthlyExpensesSummary_ShouldReturnMonthlyTotals() {
        when(userRepository.existsById(1L)).thenReturn(true);

        Expense jan = Expense.builder()
                .id(10L)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2025, 1, 5))
                .user(testUser)
                .build();

        Expense feb = Expense.builder()
                .id(11L)
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.of(2025, 2, 15))
                .user(testUser)
                .build();

        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(jan, feb));

        Map<Integer, BigDecimal> result = expenseService.getMonthlyExpensesSummary(1L, 2025);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.get(1));
        assertEquals(new BigDecimal("50.00"), result.get(2));
        assertEquals(2, result.size());
    }

    @Test
    void getMonthlyExpensesSummary_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> expenseService.getMonthlyExpensesSummary(1L, 2025));
    }

    @Test
    void getMonthlyExpensesSummary_WhenNoExpenses_ShouldReturnEmptyMap() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        Map<Integer, BigDecimal> result = expenseService.getMonthlyExpensesSummary(1L, 2025);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void getMonthlyExpensesSummary_ShouldUseFullYearRange() {
        when(userRepository.existsById(1L)).thenReturn(true);

        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        expenseService.getMonthlyExpensesSummary(1L, 2025);

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(expenseRepository).findByUserIdAndDateBetween(eq(1L), startCaptor.capture(), endCaptor.capture());

        assertEquals(LocalDate.of(2025, 1, 1), startCaptor.getValue());
        assertEquals(LocalDate.of(2025, 12, 31), endCaptor.getValue());
    }

    @Test
    void getMonthlyExpensesSummary_NullAmountTreatedAsZero() {
        when(userRepository.existsById(1L)).thenReturn(true);

        Expense withNull = Expense.builder()
                .id(20L)
                .amount(null)
                .date(LocalDate.of(2025, 6, 10))
                .user(testUser)
                .build();

        Expense withValue = Expense.builder()
                .id(21L)
                .amount(new BigDecimal("25.00"))
                .date(LocalDate.of(2025, 6, 12))
                .user(testUser)
                .build();

        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(withNull, withValue));

        Map<Integer, BigDecimal> result = expenseService.getMonthlyExpensesSummary(1L, 2025);

        assertNotNull(result);
        assertEquals(0, result.get(6).compareTo(new BigDecimal("25.00")));
        assertEquals(1, result.size());
    }

    @Test
    void getMonthlyExpensesSummary_ZeroOrNegativeTotalsOmitted() {
        when(userRepository.existsById(1L)).thenReturn(true);

        Expense marPos = Expense.builder()
                .id(30L)
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.of(2025, 3, 5))
                .user(testUser)
                .build();

        Expense marNeg = Expense.builder()
                .id(31L)
                .amount(new BigDecimal("-50.00"))
                .date(LocalDate.of(2025, 3, 10))
                .user(testUser)
                .build();


        Expense aprNeg = Expense.builder()
                .id(32L)
                .amount(new BigDecimal("-10.00"))
                .date(LocalDate.of(2025, 4, 1))
                .user(testUser)
                .build();

        Expense mayPos = Expense.builder()
                .id(33L)
                .amount(new BigDecimal("20.00"))
                .date(LocalDate.of(2025, 5, 2))
                .user(testUser)
                .build();

        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(marPos, marNeg, aprNeg, mayPos));

        Map<Integer, BigDecimal> result = expenseService.getMonthlyExpensesSummary(1L, 2025);

        assertNotNull(result);
        assertFalse(result.containsKey(3));
        assertFalse(result.containsKey(4));
        assertTrue(result.containsKey(5));
        assertEquals(0, result.get(5).compareTo(new BigDecimal("20.00")));
    }


    @Test
    void getMonthlySummary_returnsMappedDto_whenRepositoryReturnsValidRows() {
        Object[] row1 = new Object[]{"Food", new BigDecimal("100.50")};
        Object[] row2 = new Object[]{"Transport", new BigDecimal("20.00")};
        when(expenseRepository.findCategoryTotalsByYearAndMonth(2025, 10))
                .thenReturn(Arrays.asList(row1, row2));

        List<MonthlySummaryDto> result = expenseService.getMonthlySummary(2025, 10);

        assertNotNull(result);
        assertEquals(2, result.size());

        MonthlySummaryDto first = result.get(0);
        assertEquals("Food", first.getCategory());
        assertEquals(new BigDecimal("100.50"), first.getTotal());

        MonthlySummaryDto second = result.get(1);
        assertEquals("Transport", second.getCategory());
        assertEquals(new BigDecimal("20.00"), second.getTotal());

        verify(expenseRepository, times(1)).findCategoryTotalsByYearAndMonth(2025, 10);
    }

    @Test
    void getMonthlySummary_convertsDifferentNumericTypes_toBigDecimal() {

        Object[] r1 = new Object[]{"Groceries", 45.5};
        Object[] r2 = new Object[]{"Bills", 100L};
        Object[] r3 = new Object[]{"Coffee", 3};
        Object[] r4 = new Object[]{"Misc", "12.34"};
        when(expenseRepository.findCategoryTotalsByYearAndMonth(2025, 9))
                .thenReturn(Arrays.asList(r1, r2, r3, r4));

        List<MonthlySummaryDto> result = expenseService.getMonthlySummary(2025, 9);

        assertEquals(4, result.size());
        assertEquals(new BigDecimal("45.5"), result.get(0).getTotal());
        assertEquals(new BigDecimal("100"), result.get(1).getTotal());
        assertEquals(new BigDecimal("3"), result.get(2).getTotal());
        assertEquals(new BigDecimal("12.34"), result.get(3).getTotal());
    }

    @Test
    void getMonthlySummary_handlesNullCategory_asUncategorized() {
        Object[] r = new Object[]{null, new BigDecimal("5.00")};
        when(expenseRepository.findCategoryTotalsByYearAndMonth(2024, 12))
                .thenReturn(Collections.singletonList(r));

        List<MonthlySummaryDto> result = expenseService.getMonthlySummary(2024, 12);

        assertEquals(1, result.size());
        MonthlySummaryDto dto = result.get(0);
        assertEquals("Uncategorized", dto.getCategory());
        assertEquals(new BigDecimal("5.00"), dto.getTotal());
    }

    @Test
    void getMonthlySummary_returnsEmptyList_whenRepositoryReturnsEmpty() {
        when(expenseRepository.findCategoryTotalsByYearAndMonth(2023, 1))
                .thenReturn(Collections.emptyList());

        List<MonthlySummaryDto> result = expenseService.getMonthlySummary(2023, 1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getMonthlySummary_propagatesException_whenRepositoryThrows() {
        when(expenseRepository.findCategoryTotalsByYearAndMonth(2022, 7))
                .thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> expenseService.getMonthlySummary(2022, 7));
        assertEquals("DB error", ex.getMessage());
    }

    @Test
    void getMonthlySummary_throws_whenTotalIsNull_inRow() {
        Object[] r = new Object[]{"Some", null};
        when(expenseRepository.findCategoryTotalsByYearAndMonth(2025, 11))
                .thenReturn(Collections.singletonList(r));

        assertThrows(NullPointerException.class, () -> expenseService.getMonthlySummary(2025, 11));
    }

    @Test
    void getMonthlySummary_invokesRepository_withProvidedParams() {
        when(expenseRepository.findCategoryTotalsByYearAndMonth(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        expenseService.getMonthlySummary(2000, 2);
        verify(expenseRepository, times(1)).findCategoryTotalsByYearAndMonth(2000, 2);
    }

}
