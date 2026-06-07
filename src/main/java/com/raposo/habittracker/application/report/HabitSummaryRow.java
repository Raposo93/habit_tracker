package com.raposo.habittracker.application.report;

public record HabitSummaryRow(
                String habit,
                double previousPeriodScore,
                double currentPeriodScore,
                double delta,
                Trend trend,
                int previousRecordedDays,
                int previousMissingDays,
                int currentRecordedDays,
                int currentMissingDays) {
}
