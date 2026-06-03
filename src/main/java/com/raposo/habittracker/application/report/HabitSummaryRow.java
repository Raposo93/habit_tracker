package com.raposo.habittracker.application.report;

public record HabitSummaryRow(
        String habit,
        double averageScore,
        int recordedDays,
        int missingDays) {
}
