package com.raposo.habittracker.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DateRangeTest {

    @Test
    void givenEndDateBeforeStartDateWhenCreateThenThrowIllegalArgumentException() {
        LocalDate startDate = LocalDate.of(2026, 5, 25);
        LocalDate endDate = LocalDate.of(2026, 5, 24);

        assertThrows(IllegalArgumentException.class,
                () -> DateRange.of(startDate, endDate));
    }

    @Test
    void givenSameStartAndEndDateWhenCreateThenAccept() {
        DateRange range = DateRange.of(
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 25));

        assertEquals(1, range.daysInclusive());
    }

    @Test
    void givenDateInsideRangeWhenContainsThenReturnTrue() {
        DateRange range = DateRange.of(
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 31));

        assertTrue(range.contains(LocalDate.of(2026, 5, 28)));
    }

    @Test
    void givenStartDateWhenContainsThenReturnTrue() {
        DateRange range = DateRange.of(
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 31));

        assertTrue(range.contains(LocalDate.of(2026, 5, 25)));
    }

    @Test
    void givenEndDateWhenContainsThenReturnTrue() {
        DateRange range = DateRange.of(
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 31));

        assertTrue(range.contains(LocalDate.of(2026, 5, 31)));
    }

    @Test
    void givenDateOutsideRangeWhenContainsThenReturnFalse() {
        DateRange range = DateRange.of(
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 31));

        assertFalse(range.contains(LocalDate.of(2026, 6, 1)));
    }

    @Test
    void givenReferenceDateWhenWeekOfThenReturnMondayToSundayRange() {
        DateRange range = DateRange.weekOf(LocalDate.of(2026, 5, 28));

        assertEquals(LocalDate.of(2026, 5, 25), range.startDate());
        assertEquals(LocalDate.of(2026, 5, 31), range.endDate());
        assertEquals(7, range.daysInclusive());
    }

    @Test
    void givenSundayWhenWeekOfThenReturnSameWeekMondayToSundayRange() {
        DateRange range = DateRange.weekOf(LocalDate.of(2026, 5, 31));

        assertEquals(LocalDate.of(2026, 5, 25), range.startDate());
        assertEquals(LocalDate.of(2026, 5, 31), range.endDate());
    }

    @Test
    void givenRangeWhenPreviousEquivalentThenReturnPreviousRangeWithSameLength() {
        DateRange range = DateRange.of(
                LocalDate.of(2026, 5, 31),
                LocalDate.of(2026, 6, 3));

        DateRange previousRange = range.previousEquivalent();

        assertEquals(LocalDate.of(2026, 5, 27), previousRange.startDate());
        assertEquals(LocalDate.of(2026, 5, 30), previousRange.endDate());
        assertEquals(range.daysInclusive(), previousRange.daysInclusive());
    }
}
