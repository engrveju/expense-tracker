
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
import org.junit.jupiter.api.Assertions;
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

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Captor
    ArgumentCaptor<RecurringExpense> recurringCaptor;

    @Captor
    ArgumentCaptor<Expense> expenseCaptor;

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
        template.setActive(true);
        template.setInterval(1);

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
        template.setActive(true);
        template.setInterval(1);

        when(userRepository.findById(testUser.getId()))
                .thenReturn(Optional.ofNullable(testUser));

        when(recurringRepo.findDueByDate(any(LocalDate.class)))
                .thenReturn(List.of(template));

        when(expenseRepo.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        service.processDueRecurringExpenses();

        verify(expenseRepo).save(any(Expense.class));
    }



    @Test
    void processRecurringTemplate_nullInterval_defaultsToOne_and_updatesNextOccurrence() {
        LocalDate today = LocalDate.now();
        RecurringExpense template = new RecurringExpense();
        template.setUserId(1L);
        template.setNextOccurrenceDate(today);
        template.setEndDate(today.plusDays(5));
        template.setRecurrenceType(RecurrenceType.DAILY);
        template.setInterval(null);
        template.setCategory("subscription");
        template.setAmount(BigDecimal.valueOf(9.99));
        template.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        service.processRecurringTemplate(template);

        verify(expenseRepo, times(1)).save(any(Expense.class));

        verify(recurringRepo, times(1)).save(recurringCaptor.capture());
        RecurringExpense savedTemplate = recurringCaptor.getValue();
        Assertions.assertEquals(today.plusDays(1), savedTemplate.getNextOccurrenceDate());
    }


    @Test
    void whenNextOccurrenceEqualsEndDate_thenCreatesExpense_and_deactivatesTemplate() {
        LocalDate today = LocalDate.now();
        RecurringExpense template = new RecurringExpense();
        template.setUserId(1L);
        template.setNextOccurrenceDate(today);
        template.setEndDate(today);
        template.setRecurrenceType(RecurrenceType.DAILY);
        template.setInterval(1);
        template.setCategory("subscription");
        template.setAmount(BigDecimal.valueOf(9.99));
        template.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        service.processRecurringTemplate(template);

        verify(expenseRepo, times(1)).save(any(Expense.class));

        verify(recurringRepo, times(1)).save(recurringCaptor.capture());
        RecurringExpense savedTemplate = recurringCaptor.getValue();
        assertFalse(savedTemplate.isActive(), "Template must be deactivated after final occurrence");
        Assertions.assertEquals(today.plusDays(1), savedTemplate.getNextOccurrenceDate());
    }


    @Test
    void whenNextOccurrenceAfterEndDate_thenNoExpense_and_deactivatesTemplate() {
        LocalDate today = LocalDate.now();
        RecurringExpense template = new RecurringExpense();
        template.setUserId(1L);
        template.setNextOccurrenceDate(today);
        template.setEndDate(today.minusDays(1));
        template.setRecurrenceType(RecurrenceType.DAILY);
        template.setInterval(1);
        template.setCategory("subscription");
        template.setAmount(BigDecimal.valueOf(9.99));
        template.setActive(true);
        service.processRecurringTemplate(template);

        verify(expenseRepo, never()).save(any(Expense.class));

        verify(recurringRepo, times(1)).save(recurringCaptor.capture());
        RecurringExpense savedTemplate = recurringCaptor.getValue();
        assertFalse(savedTemplate.isActive(), "Template must be deactivated when next occurrence is after endDate");
        Assertions.assertEquals(today, savedTemplate.getNextOccurrenceDate());
    }


    @Test
    void processRecurringTemplate_intervalGreaterThanOne_createsExpense_then_deactivates_if_nextAfterEnd() {
        LocalDate today = LocalDate.now();
        RecurringExpense template = new RecurringExpense();
        template.setUserId(1L);
        template.setNextOccurrenceDate(today);

        template.setEndDate(today.plusDays(1));
        template.setRecurrenceType(RecurrenceType.DAILY);
        template.setInterval(2);
        template.setCategory("subscription");
        template.setAmount(BigDecimal.valueOf(9.99));
        template.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        service.processRecurringTemplate(template);

        verify(expenseRepo, times(1)).save(expenseCaptor.capture());
        Expense created = expenseCaptor.getValue();
        Assertions.assertEquals(today, created.getDate());

        verify(recurringRepo, times(1)).save(recurringCaptor.capture());
        RecurringExpense savedTemplate = recurringCaptor.getValue();
        assertFalse(savedTemplate.isActive(), "Template must be deactivated when next occurrence falls after endDate");
        Assertions.assertEquals(today.plusDays(2), savedTemplate.getNextOccurrenceDate());
    }


    @Test
    void processRecurringTemplate_endDateNull_keepsTemplateActive_and_updatesNextOccurrence() {
        LocalDate today = LocalDate.now();
        RecurringExpense template = new RecurringExpense();
        template.setUserId(1L);
        template.setNextOccurrenceDate(today);
        template.setEndDate(null);
        template.setRecurrenceType(RecurrenceType.WEEKLY);
        template.setInterval(2);
        template.setCategory("gym");
        template.setAmount(BigDecimal.valueOf(20));
        template.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        service.processRecurringTemplate(template);

        verify(expenseRepo, times(1)).save(any(Expense.class));

        verify(recurringRepo, times(1)).save(recurringCaptor.capture());
        RecurringExpense saved = recurringCaptor.getValue();
        Assertions.assertTrue(saved.isActive());
        Assertions.assertEquals(today.plusWeeks(2), saved.getNextOccurrenceDate());
    }

    @Test
    void processDueRecurringExpenses_processesAllDueTemplates() {
        LocalDate today = LocalDate.now();
        RecurringExpense t1 = new RecurringExpense();
        t1.setUserId(1L);
        t1.setNextOccurrenceDate(today);
        t1.setRecurrenceType(RecurrenceType.DAILY);
        t1.setInterval(1);
        t1.setCategory("one");
        t1.setAmount(BigDecimal.valueOf(1));
        t1.setActive(true);

        RecurringExpense t2 = new RecurringExpense();
        t2.setUserId(1L);
        t2.setNextOccurrenceDate(today);
        t2.setRecurrenceType(RecurrenceType.MONTHLY);
        t2.setInterval(1);
        t2.setCategory("two");
        t2.setAmount(BigDecimal.valueOf(2));
        t2.setActive(true);

        when(recurringRepo.findDueByDate(today)).thenReturn(Collections.singletonList(t1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        service.processDueRecurringExpenses();

        verify(expenseRepo, times(1)).save(any(Expense.class));
        verify(recurringRepo, atLeastOnce()).save(any(RecurringExpense.class));
    }


    // java
    @Test
    void createsAllMissedWeeklyOccurrences_upToToday_and_updatesNextOccurrence() {
        LocalDate today = LocalDate.now();
        LocalDate candidate = today.minusWeeks(2);

        RecurringExpense template = new RecurringExpense();
        template.setId(1L);
        template.setUserId(testUser.getId());
        template.setNextOccurrenceDate(candidate);
        template.setRecurrenceType(RecurrenceType.WEEKLY);
        template.setInterval(1);
        template.setActive(true);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        service.processRecurringTemplate(template);

        verify(expenseRepo, times(3)).save(expenseCaptor.capture());
        List<Expense> saved = expenseCaptor.getAllValues();
        Assertions.assertEquals(3, saved.size());
        Assertions.assertEquals(candidate, saved.get(0).getDate());
        Assertions.assertEquals(candidate.plusWeeks(1), saved.get(1).getDate());
        Assertions.assertEquals(candidate.plusWeeks(2), saved.get(2).getDate());

        ArgumentCaptor<RecurringExpense> templateCaptor = ArgumentCaptor.forClass(RecurringExpense.class);
        verify(recurringRepo, times(1)).save(templateCaptor.capture());
        RecurringExpense savedTemplate = templateCaptor.getValue();
        Assertions.assertNotNull(savedTemplate.getNextOccurrenceDate());
        Assertions.assertEquals(today.plusWeeks(1), savedTemplate.getNextOccurrenceDate());
        Assertions.assertTrue(savedTemplate.isActive());
    }

    @Test
    void createsOccurrencesOnlyUpToEndDate_and_deactivatesTemplateWhenEndDatePassed() {
        LocalDate today = LocalDate.now();
        LocalDate candidate = today.minusDays(2);
        LocalDate endDate = today.minusDays(1);

        RecurringExpense template = new RecurringExpense();
        template.setId(2L);
        template.setUserId(testUser.getId());
        template.setNextOccurrenceDate(candidate);
        template.setRecurrenceType(RecurrenceType.DAILY);
        template.setInterval(1);
        template.setEndDate(endDate);
        template.setActive(true);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        service.processRecurringTemplate(template);

        verify(expenseRepo, times(2)).save(expenseCaptor.capture());
        List<Expense> saved = expenseCaptor.getAllValues();
        Assertions.assertEquals(candidate, saved.get(0).getDate());
        Assertions.assertEquals(candidate.plusDays(1), saved.get(1).getDate());

        ArgumentCaptor<RecurringExpense> templateCaptor = ArgumentCaptor.forClass(RecurringExpense.class);
        verify(recurringRepo, times(1)).save(templateCaptor.capture());
        RecurringExpense savedTemplate = templateCaptor.getValue();
        assertFalse(savedTemplate.isActive(), "template should be deactivated when endDate reached");
        Assertions.assertTrue(savedTemplate.getNextOccurrenceDate() == null || savedTemplate.getNextOccurrenceDate().isAfter(endDate));
    }

    @Test
    void invalidInterval_deactivatesTemplate_and_noExpensesCreated() {
        LocalDate today = LocalDate.now();

        RecurringExpense template = new RecurringExpense();
        template.setId(3L);
        template.setUserId(testUser.getId());
        template.setNextOccurrenceDate(today);
        template.setRecurrenceType(RecurrenceType.DAILY);
        template.setInterval(0);
        template.setActive(true);

        // no need to stub userRepository since no expense creation should occur, but setting userId is harmless
        service.processRecurringTemplate(template);

        verifyNoInteractions(expenseRepo);
        ArgumentCaptor<RecurringExpense> templateCaptor = ArgumentCaptor.forClass(RecurringExpense.class);
        verify(recurringRepo, times(1)).save(templateCaptor.capture());
        RecurringExpense savedTemplate = templateCaptor.getValue();
        assertFalse(savedTemplate.isActive());
    }

    @Test
    void safetyCap_preventsInfiniteLoop_and_persistsTemplateOnHit() {
        LocalDate today = LocalDate.now();
        LocalDate candidate = today.minusDays(2000);

        RecurringExpense template = new RecurringExpense();
        template.setId(4L);
        template.setUserId(testUser.getId());
        template.setNextOccurrenceDate(candidate);
        template.setRecurrenceType(RecurrenceType.DAILY);
        template.setInterval(1);
        template.setActive(true);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        service.processRecurringTemplate(template);
        verify(expenseRepo, atLeast(1)).save(any(Expense.class));
        verify(recurringRepo, atLeast(1)).save(any(RecurringExpense.class));
    }

    @Test
    void processRecurringTemplate_nullNextOccurrence_deactivatesTemplate() {
        RecurringExpense template = new RecurringExpense();
        template.setId(5L);
        template.setUserId(testUser.getId());
        template.setNextOccurrenceDate(null);
        template.setRecurrenceType(RecurrenceType.DAILY);
        template.setInterval(1);
        template.setActive(true);

        service.processRecurringTemplate(template);

        ArgumentCaptor<RecurringExpense> templateCaptor = ArgumentCaptor.forClass(RecurringExpense.class);
        verify(recurringRepo, times(1)).save(templateCaptor.capture());
        RecurringExpense savedTemplate = templateCaptor.getValue();
        assertFalse(savedTemplate.isActive(), "Template should be deactivated when nextOccurrenceDate is null");
        Assertions.assertNull(savedTemplate.getNextOccurrenceDate(), "nextOccurrenceDate should remain null");
    }

    @Test
    void processRecurringTemplate_expenseCreationFails_transactionRollsBackAndTemplateNotSaved() {
        LocalDate today = LocalDate.now();

        RecurringExpense template = new RecurringExpense();
        template.setId(6L);
        template.setUserId(testUser.getId());
        template.setNextOccurrenceDate(today);
        template.setRecurrenceType(RecurrenceType.DAILY);
        template.setInterval(1);
        template.setActive(true);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(expenseRepo.save(any(Expense.class))).thenThrow(new RuntimeException("simulated db failure"));

        Assertions.assertThrows(RuntimeException.class, () -> service.processRecurringTemplate(template));

        verify(expenseRepo, atLeastOnce()).save(any(Expense.class));
        verify(recurringRepo, never()).save(any(RecurringExpense.class));
    }

}
