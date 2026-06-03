package com.raposo.habittracker.cli.formatter;

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
        output.append("- Week starts on Monday and ends on Sunday\n");
        output.append("- Missing score means no data, not zero\n\n");

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
        output.append("| habit | previous_avg | current_avg | delta | trend |\n");
        output.append("|-------|--------------|-------------|-------|-------|\n");

        for (HabitSummaryRow row : report.summary()) {
            output.append("| ")
                    .append(row.habit())
                    .append(" | ")
                    .append(row.previousAvg())
                    .append(" | ")
                    .append(row.currentAvg())
                    .append(" | ")
                    .append(formatDelta(row.delta()))
                    .append(" | ")
                    .append(formatTrend(row.trend()))
                    .append(" |\n");
        }

        return output.toString();
    }

    private String formatDelta(double delta) {
        return delta > 0 ? "+" + delta : String.valueOf(delta);
    }

    private String formatTrend(Trend trend) {
        return switch (trend) {
            case IMPROVED -> "↑ improved";
            case WORSENED -> "↓ worsened";
            case STABLE -> "→ stable";
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
}
