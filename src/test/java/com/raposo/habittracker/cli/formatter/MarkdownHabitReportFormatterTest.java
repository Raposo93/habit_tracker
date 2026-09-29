package com.raposo.habittracker.cli.formatter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.report.EntryReportRow;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.application.report.HabitSummaryRow;
import com.raposo.habittracker.application.report.ReportContext;
import com.raposo.habittracker.application.report.Trend;
import com.raposo.habittracker.domain.DateRange;

class MarkdownHabitReportFormatterTest {
    @Test
    void separatesSectionsAndPreservesTableValuesAndPunctuation() {
        LocalDate date = LocalDate.of(2026, 9, 2);
        HabitReport report = new HabitReport(
                new ReportContext("0 to 3", "Monday to Sunday", "Missing is not zero"),
                DateRange.of(date, date),
                DateRange.of(date.minusDays(1), date.minusDays(1)),
                List.of(new EntryReportRow(date, "Wednesday", date.minusDays(2), "Sleep", 0, "Tired: yes, but okay. Tea | rest")),
                List.of(new HabitSummaryRow("Sleep", 0, 0, 0, Trend.NO_BASELINE, 0, 0, 1, 0)));

        String markdown = new MarkdownHabitReportFormatter().format(report);

        assertTrue(markdown.startsWith("## Context\n\n- Score scale: 0 to 3\n"));
        assertTrue(markdown.contains("\n\n## Entries\n\n| date | weekday | week_start | habit | score | note |\n"
                + "| --- | --- | --- | --- | --- | --- |\n"
                + "| 2026-09-02 | Wednesday | 2026-08-31 | Sleep | 0.00 | Tired: yes, but okay. Tea \\| rest |\n"));
        assertTrue(markdown.contains("\n\n## Summary\n\n| habit | previous_score | current_score | delta | trend |"));
        assertTrue(markdown.contains("| Sleep | N/A | 0.00 | N/A | ∅ no baseline | 0 | 0 | 1 | 0 |\n"));
    }
}
