package com.ugwueze.expenses_tracker.service;

import com.ugwueze.expenses_tracker.entity.Expense;
import com.ugwueze.expenses_tracker.entity.User;
import com.ugwueze.expenses_tracker.exception.ResourceNotFoundException;
import com.ugwueze.expenses_tracker.repository.ExpenseRepository;
import com.ugwueze.expenses_tracker.repository.UserRepository;
import com.ugwueze.expenses_tracker.service.impl.ExpenseExportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseExportServiceImplTest {

    private ExpenseRepository expenseRepository;
    private UserRepository userRepository;
    private ExpenseExportServiceImpl service;

    @BeforeEach
    void setup() {
        expenseRepository = mock(ExpenseRepository.class);
        userRepository = mock(UserRepository.class);
        service = new ExpenseExportServiceImpl(expenseRepository, userRepository);
    }

    private Expense makeExpense(Long id) {
        Expense e = new Expense();
        e.setId(id);
        e.setDate(LocalDate.of(2023, 1, 2));
        e.setDescription("Desc,with,comma");
        e.setCategory("Cat\"Quote");
        e.setPaymentMethod(null);
        e.setAmount(new BigDecimal("123.45"));
        e.setNotes("note\nnewline");
        e.setCreatedAt(LocalDateTime.of(2023,1,2,3,4,5));
        e.setUpdatedAt(LocalDateTime.of(2023,1,3,4,5,6));
        return e;
    }

    @Test
    void streamExpensesAsCsv_withDateRange_usesFindByUserIdAndDateBetween_andWritesCsv() throws Exception {
        Long userId = 10L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Expense e1 = makeExpense(1L);
        when(expenseRepository.findByUserIdAndDateBetween(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(e1));

        StringWriter writer = new StringWriter();
        service.streamExpensesAsCsv(userId, LocalDate.of(2023,1,1), LocalDate.of(2023,12,31), writer);

        String output = writer.toString();
        assertTrue(output.contains("id,date,description,category,payment_method,amount,notes,created_at,updated_at"));
        assertTrue(output.contains("1"));
        assertTrue(output.contains("123.45"));
        assertTrue(output.contains("note\nnewline"));
        verify(expenseRepository, times(1)).findByUserIdAndDateBetween(eq(userId), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void streamExpensesAsCsv_withStartOnly_usesFindByUserIdAndDateGreaterThanEqual() throws Exception {
        Long userId = 11L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Expense e = makeExpense(2L);
        when(expenseRepository.findByUserIdAndDateGreaterThanEqual(eq(userId), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(e)));

        StringWriter writer = new StringWriter();
        service.streamExpensesAsCsv(userId, LocalDate.of(2023,1,1), null, writer);

        String output = writer.toString();
        assertTrue(output.contains("2"));
        verify(expenseRepository, times(1)).findByUserIdAndDateGreaterThanEqual(eq(userId), any(LocalDate.class), any(Pageable.class));
    }

    @Test
    void streamExpensesAsCsv_withEndOnly_usesFindByUserIdAndDateLessThanEqual() throws Exception {
        Long userId = 12L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Expense e = makeExpense(3L);
        when(expenseRepository.findByUserIdAndDateLessThanEqual(eq(userId), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(e)));

        StringWriter writer = new StringWriter();
        service.streamExpensesAsCsv(userId, null, LocalDate.of(2023,12,31), writer);

        String output = writer.toString();
        assertTrue(output.contains("3"));
        verify(expenseRepository, times(1)).findByUserIdAndDateLessThanEqual(eq(userId), any(LocalDate.class), any(Pageable.class));
    }

    @Test
    void streamExpensesAsCsv_noFilters_usesFindByUserIdWithPageable() throws Exception {
        Long userId = 13L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Expense e = makeExpense(4L);
        when(expenseRepository.findByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(e)));

        StringWriter writer = new StringWriter();
        service.streamExpensesAsCsv(userId, null, null, writer);

        String output = writer.toString();
        assertTrue(output.contains("4"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(expenseRepository).findByUserId(eq(userId), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertNotNull(pageable);
    }

    @Test
    void streamExpensesAsCsv_userNotFound_throwsResourceNotFoundException() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.streamExpensesAsCsv(userId, null, null, new StringWriter());
        });

        verifyNoInteractions(expenseRepository);
    }
}
