package com.raposo.habittracker.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

public record DateRange(LocalDate startDate, LocalDate endDate) {

    public DateRange {
        Objects.requireNonNull(startDate, "startDate cannot be null");
        Objects.requireNonNull(endDate, "endDate cannot be null");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }
    }

    public static DateRange of(LocalDate startDate, LocalDate endDate) {
        return new DateRange(startDate, endDate);
    }

    public static DateRange weekOf(LocalDate referenceDate) {
        Objects.requireNonNull(referenceDate, "referenceDate cannot be null");

        LocalDate startOfWeek = referenceDate.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        return new DateRange(startOfWeek, startOfWeek.plusDays(6));
    }

    public long daysInclusive() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date, "date cannot be null");

        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public DateRange previousEquivalent() {
        long days = daysInclusive();

        return new DateRange(
                startDate.minusDays(days),
                endDate.minusDays(days)
        );
    }
}