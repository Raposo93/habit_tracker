package com.raposo.habittracker;

import java.time.LocalDate;

public record DateRange(
        LocalDate startDate,
        LocalDate endDate
) {
    public static DateRange between(String startDate, String endDate) {
        return new DateRange(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
    }
}