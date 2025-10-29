package com.ugwueze.expenses_tracker.service;

import com.ugwueze.expenses_tracker.dto.BudgetDto;
import com.ugwueze.expenses_tracker.entity.Budget;
import com.ugwueze.expenses_tracker.entity.User;
import com.ugwueze.expenses_tracker.exception.ResourceNotFoundException;
import com.ugwueze.expenses_tracker.repository.BudgetRepository;
import com.ugwueze.expenses_tracker.repository.UserRepository;
import com.ugwueze.expenses_tracker.service.impl.BudgetServiceImpl;
import com.ugwueze.expenses_tracker.util.BudgetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private BudgetMapper budgetMapper;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    @Captor
    private ArgumentCaptor<Budget> budgetCaptor;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
    }

    @Test
    void createBudget_success() {
        BudgetDto dto = BudgetDto.builder()
                .id(null)
                .userId(1L)
                .category("Food")
                .amount(new BigDecimal("100.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(5))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Budget saved = new Budget();
        saved.setId(10L);
        saved.setCategory(dto.getCategory());
        saved.setAmount(dto.getAmount());
        saved.setStartDate(dto.getStartDate());
        saved.setEndDate(dto.getEndDate());
        saved.setUser(user);

        when(budgetRepository.save(any(Budget.class))).thenReturn(saved);

        BudgetDto returnedDto = BudgetDto.builder()
                .id(10L)
                .userId(1L)
                .category("Food")
                .amount(new BigDecimal("100.00"))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

        when(budgetMapper.toDto(saved)).thenReturn(returnedDto);

        BudgetDto result = budgetService.createBudget(dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Food", result.getCategory());
        verify(userRepository, times(1)).findById(1L);
        verify(budgetRepository, times(1)).save(budgetCaptor.capture());
        Budget captured = budgetCaptor.getValue();
        assertEquals("Food", captured.getCategory());
        assertEquals(dto.getAmount(), captured.getAmount());
    }

    @Test
    void createBudget_nullDto_throws() {
        assertThrows(IllegalArgumentException.class, () -> budgetService.createBudget(null));
    }

    @Test
    void updateBudget_success() {
        Long id = 5L;
        Budget existing = new Budget();
        existing.setId(id);
        existing.setCategory("Old");
        User existingUser = new User();
        existingUser.setId(2L);
        existing.setUser(existingUser);

        BudgetDto dto = BudgetDto.builder()
                .id(id)
                .userId(2L)
                .category("New")
                .amount(new BigDecimal("250.00"))
                .startDate(LocalDate.now().minusDays(2))
                .endDate(LocalDate.now().plusDays(2))
                .build();

        when(budgetRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.findById(2L)).thenReturn(Optional.of(existingUser));

        Budget updated = new Budget();
        updated.setId(id);
        updated.setCategory(dto.getCategory());
        updated.setAmount(dto.getAmount());
        updated.setStartDate(dto.getStartDate());
        updated.setEndDate(dto.getEndDate());
        updated.setUser(existingUser);

        when(budgetRepository.save(existing)).thenReturn(updated);

        BudgetDto returnedDto = BudgetDto.builder()
                .id(id)
                .userId(2L)
                .category("New")
                .amount(dto.getAmount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

        when(budgetMapper.toDto(updated)).thenReturn(returnedDto);

        BudgetDto result = budgetService.updateBudget(id, dto);

        assertNotNull(result);
        assertEquals("New", result.getCategory());
        assertEquals(dto.getAmount(), result.getAmount());
        verify(budgetRepository, times(1)).findById(id);
        verify(userRepository, times(1)).findById(2L);
        verify(budgetRepository, times(1)).save(existing);
    }

    @Test
    void updateBudget_userMismatch_throws() {
        Long id = 5L;
        Budget existing = new Budget();
        existing.setId(id);
        User existingUser = new User();
        existingUser.setId(99L);
        existing.setUser(existingUser);

        BudgetDto dto = BudgetDto.builder()
                .id(id)
                .userId(2L)
                .category("X")
                .amount(new BigDecimal("10"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();

        when(budgetRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(ResourceNotFoundException.class, () -> budgetService.updateBudget(id, dto));
        verify(budgetRepository).findById(id);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteBudget_success() {
        Long id = 7L;
        Long userId = 3L;
        Budget existing = new Budget();
        existing.setId(id);
        User u = new User();
        u.setId(userId);
        existing.setUser(u);

        when(budgetRepository.findById(id)).thenReturn(Optional.of(existing));

        budgetService.deleteBudget(id, userId);

        verify(budgetRepository, times(1)).findById(id);
        verify(budgetRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteBudget_userMismatch_throws() {
        Long id = 8L;
        Long userId = 4L;
        Budget existing = new Budget();
        existing.setId(id);
        User u = new User();
        u.setId(999L);
        existing.setUser(u);

        when(budgetRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(ResourceNotFoundException.class, () -> budgetService.deleteBudget(id, userId));
        verify(budgetRepository).findById(id);
        verify(budgetRepository, never()).deleteById(anyLong());
    }

    @Test
    void getBudgetById_success() {
        Long id = 11L;
        Long userId = 6L;
        Budget budget = new Budget();
        budget.setId(id);
        User u = new User();
        u.setId(userId);
        budget.setUser(u);

        when(budgetRepository.findById(id)).thenReturn(Optional.of(budget));

        BudgetDto dto = BudgetDto.builder()
                .id(id)
                .userId(userId)
                .category("Transport")
                .amount(new BigDecimal("50"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .build();

        when(budgetMapper.toDto(budget)).thenReturn(dto);

        BudgetDto result = budgetService.getBudgetById(id, userId);

        assertNotNull(result);
        assertEquals("Transport", result.getCategory());
        verify(budgetRepository).findById(id);
    }

    @Test
    void getUserBudgets_success() {
        Long userId = 2L;
        when(userRepository.existsById(userId)).thenReturn(true);

        Budget b1 = new Budget();
        b1.setId(1L);
        b1.setCategory("Food");
        b1.setUser(user);
        Budget b2 = new Budget();
        b2.setId(2L);
        b2.setCategory("Fuel");
        b2.setUser(user);

        when(budgetRepository.findByUserId(userId)).thenReturn(List.of(b1, b2));

        when(budgetMapper.toDto(b1)).thenReturn(BudgetDto.builder().id(1L).userId(userId).category("Food").build());
        when(budgetMapper.toDto(b2)).thenReturn(BudgetDto.builder().id(2L).userId(userId).category("Fuel").build());

        var result = budgetService.getUserBudgets(userId);

        assertEquals(2, result.size());
        verify(userRepository).existsById(userId);
        verify(budgetRepository).findByUserId(userId);
    }

    @Test
    void getCurrentBudgetForCategory_found() {
        Long userId = 3L;
        String category = "Groceries";
        when(userRepository.existsById(userId)).thenReturn(true);

        LocalDate today = LocalDate.now();
        Budget b = new Budget();
        b.setId(21L);
        b.setCategory("Groceries");
        b.setStartDate(today.minusDays(1));
        b.setEndDate(today.plusDays(1));
        b.setUser(user);

        when(budgetRepository.findByUserId(userId)).thenReturn(List.of(b));

        BudgetDto dto = BudgetDto.builder()
                .id(21L)
                .userId(userId)
                .category("Groceries")
                .startDate(b.getStartDate())
                .endDate(b.getEndDate())
                .amount(new BigDecimal("120.00"))
                .build();

        when(budgetMapper.toDto(b)).thenReturn(dto);

        BudgetDto result = budgetService.getCurrentBudgetForCategory(userId, category);

        assertNotNull(result);
        assertEquals("Groceries", result.getCategory());
        verify(budgetRepository).findByUserId(userId);
    }

    @Test
    void getCurrentBudgetForCategory_notFound_returnsNull() {
        Long userId = 4L;
        String category = "NonExisting";
        when(userRepository.existsById(userId)).thenReturn(true);
        when(budgetRepository.findByUserId(userId)).thenReturn(List.of());

        BudgetDto result = budgetService.getCurrentBudgetForCategory(userId, category);
        assertNull(result);
    }

    @Test
    void isBudgetExceeded_true() {
        Long userId = 9L;
        String category = "Bills";

        LocalDate today = LocalDate.now();
        Budget b = new Budget();
        b.setId(30L);
        b.setCategory("Bills");
        b.setStartDate(today.minusDays(5));
        b.setEndDate(today.plusDays(5));
        b.setUser(user);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(budgetRepository.findByUserId(userId)).thenReturn(List.of(b));

        BudgetDto dto = BudgetDto.builder()
                .id(30L)
                .userId(userId)
                .category("Bills")
                .startDate(b.getStartDate())
                .endDate(b.getEndDate())
                .amount(new BigDecimal("200.00"))
                .build();

        when(budgetMapper.toDto(b)).thenReturn(dto);

        when(expenseService.getTotalExpensesByDateRange(eq(userId), eq(b.getStartDate()), eq(b.getEndDate())))
                .thenReturn(new BigDecimal("250.00"));

        boolean exceeded = budgetService.isBudgetExceeded(userId, category);
        assertTrue(exceeded);
    }

    @Test
    void isBudgetExceeded_noBudget_returnsFalse() {
        Long userId = 12L;
        String category = "Misc";
        when(userRepository.existsById(userId)).thenReturn(true);
        when(budgetRepository.findByUserId(userId)).thenReturn(List.of());

        boolean exceeded = budgetService.isBudgetExceeded(userId, category);
        assertFalse(exceeded);
    }
}
