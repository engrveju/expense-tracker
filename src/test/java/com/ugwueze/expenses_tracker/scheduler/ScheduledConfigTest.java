package com.ugwueze.expenses_tracker.scheduler;

import com.ugwueze.expenses_tracker.service.RecurringExpenseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ScheduledConfigTest {

    @Mock
    private RecurringExpenseService recurringExpenseService;

    @InjectMocks
    private ScheduledConfig scheduledConfig;

    @Test
    void runRecurringTemplateJob_callsProcessDueRecurringExpenses() {
        scheduledConfig.runRecurringTemplateJob();

        verify(recurringExpenseService).processDueRecurringExpenses();
        verifyNoMoreInteractions(recurringExpenseService);
    }
}
