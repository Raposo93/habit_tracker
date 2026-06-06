package com.raposo.habittracker.application.report;

public record HabitSummaryRow(
        String habit,
        double previousAverageScore,
        double currentAverageScore,
        double delta,
        Trend trend,
        int currentRecordedDays,
        int currentMissingDays
) {
}
