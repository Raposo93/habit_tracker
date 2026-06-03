package com.raposo.habittracker.application.report;

public record HabitSummaryRow(
        String habit,
        double previousAvg,
        double currentAvg,
        double delta,
        Trend trend
) {
}
