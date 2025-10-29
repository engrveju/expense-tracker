package com.ugwueze.expenses_tracker.utils;

import com.ugwueze.expenses_tracker.enums.RecurrenceType;
import com.ugwueze.expenses_tracker.util.RecurrenceUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RecurrenceUtilsTest {

    @Test
    void nextOccurrence_monthly_lastDay_fromApr30_toMay31() {
        LocalDate current = LocalDate.of(2025, 4, 30);
        LocalDate next = RecurrenceUtils.nextOccurrence(current, RecurrenceType.MONTHLY, 1);
        assertEquals(LocalDate.of(2025, 5, 31), next);
    }

    @Test
    void nextOccurrence_monthly_lastDay_multiMonth_preservesLastDay_Jan31_plus2_toMar31() {
        LocalDate current = LocalDate.of(2025, 1, 31);
        LocalDate next = RecurrenceUtils.nextOccurrence(current, RecurrenceType.MONTHLY, 2);
        assertEquals(LocalDate.of(2025, 3, 31), next);
    }

    @Test
    void isOccurrenceWithinEndDate_inclusive_and_exclusive_behavior() {
        LocalDate occ = LocalDate.of(2025, 10, 29);
        LocalDate endAfter = occ.plusDays(1);
        LocalDate endBefore = occ.minusDays(1);

        assertTrue(RecurrenceUtils.isOccurrenceWithinEndDate(occ, occ), "occurrence on endDate must be allowed (inclusive)");
        assertTrue(RecurrenceUtils.isOccurrenceWithinEndDate(occ, endAfter), "occurrence before endDate must be allowed");
        assertFalse(RecurrenceUtils.isOccurrenceWithinEndDate(occ, endBefore), "occurrence after endDate must not be allowed");
    }

    @Test
    void nextOccurrence_nullCurrent_throwsNpe() {
        assertThrows(NullPointerException.class, () ->
                RecurrenceUtils.nextOccurrence(null, RecurrenceType.DAILY, 1));
    }

    @Test
    void nextOccurrence_invalidInterval_throwsIllegalArgumentException() {
        LocalDate current = LocalDate.now();
        assertThrows(IllegalArgumentException.class, () ->
                RecurrenceUtils.nextOccurrence(current, RecurrenceType.DAILY, 0));
        assertThrows(IllegalArgumentException.class, () ->
                RecurrenceUtils.nextOccurrence(current, RecurrenceType.DAILY, -5));
    }

    @Test
    void nextOccurrence_monthly_fromJan31_toFeb28_nonLeapYear() {
        LocalDate current = LocalDate.of(2025, 1, 31);
        LocalDate next = RecurrenceUtils.nextOccurrence(current, RecurrenceType.MONTHLY, 1);
        assertEquals(LocalDate.of(2025, 2, 28), next);
    }

    @Test
    void nextOccurrence_monthly_fromJan31_toFeb29_leapYear() {
        LocalDate current = LocalDate.of(2024, 1, 31);
        LocalDate next = RecurrenceUtils.nextOccurrence(current, RecurrenceType.MONTHLY, 1);
        assertEquals(LocalDate.of(2024, 2, 29), next);
    }
}
