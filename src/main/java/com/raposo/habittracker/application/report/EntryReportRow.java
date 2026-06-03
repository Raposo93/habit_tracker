package com.raposo.habittracker.application.report;

import java.time.LocalDate;

public record EntryReportRow(
        LocalDate date,
        String weekday,
        LocalDate weekStart,
        String habit,
        double score,
        String note
) {
}