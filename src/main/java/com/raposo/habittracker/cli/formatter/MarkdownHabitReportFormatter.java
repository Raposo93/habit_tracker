package com.raposo.habittracker.cli.formatter;

import java.util.List;
import java.util.Locale;

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
        output.append(formatEntriesTable(report));

        output.append("\nSummary:\n");
        output.append(formatSummaryTable(report));

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

    private String formatEntriesTable(HabitReport report) {
        List<List<String>> rows = report.entries().stream()
                .map(row -> List.of(
                        row.date().toString(),
                        row.weekday(),
                        row.weekStart().toString(),
                        row.habit(),
                        formatScore(row.score()),
                        row.note()))
                .toList();

        return MarkdownTable.render(
                List.of("date", "weekday", "week_start", "habit", "score", "note"),
                rows);
    }

    private String formatSummaryTable(HabitReport report) {
        List<List<String>> rows = report.summary().stream()
                .map(row -> List.of(
                        row.habit(),
                        formatPreviousPeriodScore(row),
                        formatScore(row.currentPeriodScore()),
                        formatDelta(row),
                        formatTrend(row.trend()),
                        String.valueOf(row.previousRecordedDays()),
                        String.valueOf(row.previousMissingDays()),
                        String.valueOf(row.currentRecordedDays()),
                        String.valueOf(row.currentMissingDays())))
                .toList();

        return MarkdownTable.render(
                List.of(
                        "habit",
                        "previous_score",
                        "current_score",
                        "delta",
                        "trend",
                        "prev_recorded",
                        "prev_missing",
                        "curr_recorded",
                        "curr_missing"),
                rows);
    }
}
