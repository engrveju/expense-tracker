package com.ugwueze.expenses_tracker.util;

import com.ugwueze.expenses_tracker.enums.RecurrenceType;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

public final class RecurrenceUtils {

    private RecurrenceUtils() {
    }

    public static LocalDate nextOccurrence(LocalDate current, RecurrenceType type, int interval) {
        Objects.requireNonNull(current, "current date cannot be null");
        if (interval <= 0) {
            throw new IllegalArgumentException("interval must be > 0");
        }

        switch (type) {
            case DAILY:
                return current.plusDays(interval);
            case WEEKLY:
                return current.plusWeeks(interval);
            case MONTHLY:
                LocalDate candidate = current.plusMonths(interval);
                if (isLastDayOfMonth(current)) {
                    return candidate.with(TemporalAdjusters.lastDayOfMonth());
                }
                return candidate;
            case YEARLY:
                return current.plusYears(interval);
            default:
                throw new UnsupportedOperationException("Unknown recurrence type: " + type);
        }
    }

    private static boolean isLastDayOfMonth(LocalDate d) {
        return d.getDayOfMonth() == d.lengthOfMonth();
    }


    public static boolean isOccurrenceWithinEndDate(LocalDate occurrenceDate, LocalDate endDate) {
        if (endDate == null) return true;
        return !occurrenceDate.isAfter(endDate);
    }

}

