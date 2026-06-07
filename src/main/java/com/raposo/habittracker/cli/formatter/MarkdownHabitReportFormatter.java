package com.raposo.habittracker.cli.formatter;

import java.util.Locale;

import com.raposo.habittracker.application.report.EntryReportRow;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.application.report.HabitSummaryRow;
import com.raposo.habittracker.application.report.Trend;

public class MarkdownHabitReportFormatter implements HabitReportFormatter {

    @Override
    public String format(HabitReport report) {
        StringBuilder output = new StringBuilder();

        output.append("Context:\n");
        output.append("- Score scale: ")
                .append(report.context().scoreScale())
                .append("\n");

        output.append("- Current range: ")
                .append(report.currentRange().startDate())
                .append(" to ")
                .append(report.currentRange().endDate())
                .append("\n");

        output.append("- Previous range: ")
                .append(report.previousRange().startDate())
                .append(" to ")
                .append(report.previousRange().endDate())
                .append("\n");

        output.append("- Week starts on Monday and ends on Sunday\n");
        output.append("- Entries contain only recorded habit entries\n");
        output.append("- Missing entry means no recorded score, not an explicit 0\n");
        output.append("- score 0 is a recorded entry\n");
        output.append("- period_score = sum(recorded entry scores) / days in range\n");
        output.append("- Missing days contribute 0 to period_score\n");
        output.append("- trend is N/A when previous range has no recorded data\n\n");

        output.append("Entries:\n");
        output.append("| date | weekday | week_start | habit | score | note |\n");
        output.append("|------|---------|------------|-------|-------|------|\n");

        for (EntryReportRow row : report.entries()) {
            output.append("| ")
                    .append(row.date())
                    .append(" | ")
                    .append(row.weekday())
                    .append(" | ")
                    .append(row.weekStart())
                    .append(" | ")
                    .append(row.habit())
                    .append(" | ")
                    .append(row.score())
                    .append(" | ")
                    .append(formatCell(row.note()))
                    .append(" |\n");
        }

        output.append("\nSummary:\n");
        output.append(
                "| habit | previous_period_score | current_period_score | delta | trend | previous_recorded_days | previous_missing_days | current_recorded_days | current_missing_days |\n");
        output.append(
                "|-------|-----------------------|----------------------|-------|-------|------------------------|-----------------------|-----------------------|----------------------|\n");

        for (HabitSummaryRow row : report.summary()) {
            output.append("| ")
                    .append(row.habit())
                    .append(" | ")
                    .append(formatPreviousPeriodScore(row))
                    .append(" | ")
                    .append(formatScore(row.currentPeriodScore()))
                    .append(" | ")
                    .append(formatDelta(row))
                    .append(" | ")
                    .append(formatTrend(row.trend()))
                    .append(" | ")
                    .append(row.previousRecordedDays())
                    .append(" | ")
                    .append(row.previousMissingDays())
                    .append(" | ")
                    .append(row.currentRecordedDays())
                    .append(" | ")
                    .append(row.currentMissingDays())
                    .append(" |\n");
        }

        return output.toString();
    }

    private String formatDelta(double delta) {
        if (delta > 0) {
            return "+" + formatScore(delta);
        }

        return formatScore(delta);
    }

    private String formatTrend(Trend trend) {
        return switch (trend) {
            case IMPROVED -> "↑ improved";
            case WORSENED -> "↓ worsened";
            case STABLE -> "→ stable";
            case NO_BASELINE -> "∅ no baseline";
        };
    }

    private String formatCell(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("|", "\\|")
                .trim();
    }

    private String formatScore(double value) {
        return String.format(Locale.ENGLISH, "%.2f", value);
    }

    private String formatPreviousPeriodScore(HabitSummaryRow row) {
        if (row.previousRecordedDays() == 0) {
            return "N/A";
        }

        return formatScore(row.previousPeriodScore());
    }

    private String formatDelta(HabitSummaryRow row) {
        if (row.previousRecordedDays() == 0) {
            return "N/A";
        }

        return formatDelta(row.delta());
    }
}
