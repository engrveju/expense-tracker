
package com.ugwueze.expenses_tracker.service;

import com.ugwueze.expenses_tracker.entity.Expense;
import com.ugwueze.expenses_tracker.entity.RecurringExpense;
import com.ugwueze.expenses_tracker.entity.User;
import com.ugwueze.expenses_tracker.enums.PaymentMethod;
import com.ugwueze.expenses_tracker.enums.RecurrenceType;
import com.ugwueze.expenses_tracker.repository.ExpenseRepository;
import com.ugwueze.expenses_tracker.repository.RecurringExpenseRepository;
import com.ugwueze.expenses_tracker.repository.UserRepository;
import com.ugwueze.expenses_tracker.service.impl.RecurringExpenseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RecurringExpenseServiceTest {

    @Mock
    private RecurringExpenseRepository recurringRepo;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseRepository expenseRepo;

    @InjectMocks
    private RecurringExpenseServiceImpl service;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void whenNoDueTemplates_thenNothingHappens() {
        lenient().when(recurringRepo.findDueByDate(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        service.processDueRecurringExpenses();

        verify(expenseRepo, never()).save(any(Expense.class));
        verify(recurringRepo, never()).save(any(RecurringExpense.class));
    }

    @Test
    void whenTemplateDue_thenExpenseCreatedAndNextOccurrenceAdvanced() {
        LocalDate today = LocalDate.now();
        RecurringExpense template = new RecurringExpense();
        template.setId(1L);
        template.setUserId(testUser.getId());
        template.setCategory("Subscription");
        template.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        template.setAmount(BigDecimal.valueOf(50.0));
        template.setRecurrenceType(RecurrenceType.MONTHLY);
        template.setNextOccurrenceDate(today.minusDays(0));
        template.setEndDate(null);

        when(recurringRepo.findDueByDate(any(LocalDate.class)))
                .thenReturn(List.of(template));

        when(userRepository.findById(testUser.getId()))
                .thenReturn(Optional.ofNullable(testUser));

        when(expenseRepo.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recurringRepo.save(any(RecurringExpense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.processDueRecurringExpenses();

        ArgumentCaptor<Expense> expCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepo).save(expCaptor.capture());
        Expense saved = expCaptor.getValue();

        assertThat(saved.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(saved.getCategory()).isEqualTo("Subscription");
        assertThat(saved.getAmount()).isEqualTo(BigDecimal.valueOf(50.0));
        assertThat(saved.getDate()).isEqualTo(today);

        ArgumentCaptor<RecurringExpense> templateCaptor = ArgumentCaptor.forClass(RecurringExpense.class);
        verify(recurringRepo).save(templateCaptor.capture());
        RecurringExpense updatedTemplate = templateCaptor.getValue();
        assertThat(updatedTemplate.getNextOccurrenceDate()).isEqualTo(today.plusMonths(1));
    }

    @Test
    void whenEndDateReached_thenTemplateDeletedNotSaved() {
        LocalDate today = LocalDate.now();
        RecurringExpense template = new RecurringExpense();
        template.setId(2L);
        template.setUserId(testUser.getId());
        template.setCategory("AnnualMembership");
        template.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        template.setAmount(BigDecimal.valueOf(120.0));
        template.setRecurrenceType(RecurrenceType.YEARLY);
        template.setNextOccurrenceDate(today.minusDays(0));
        template.setEndDate(today);

        when(userRepository.findById(testUser.getId()))
                .thenReturn(Optional.ofNullable(testUser));

        when(recurringRepo.findDueByDate(any(LocalDate.class)))
                .thenReturn(List.of(template));

        when(expenseRepo.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        service.processDueRecurringExpenses();

        verify(expenseRepo).save(any(Expense.class));
        verify(recurringRepo).delete(template);
        verify(recurringRepo, never()).save(template);
    }
}
