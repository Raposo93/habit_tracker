package com.raposo.habittracker.application.report;

import java.util.List;

import com.raposo.habittracker.domain.DateRange;

public record HabitReport(
                ReportContext context,
                DateRange currentRange,
                DateRange previousRange,
                List<EntryReportRow> entries,
                List<HabitSummaryRow> summary,
                List<HabitScoringGuide> scoringGuides) {
    public HabitReport(ReportContext context, DateRange currentRange, DateRange previousRange,
            List<EntryReportRow> entries, List<HabitSummaryRow> summary) {
        this(context, currentRange, previousRange, entries, summary, List.of());
    }
}
