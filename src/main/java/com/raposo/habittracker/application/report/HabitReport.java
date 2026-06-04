package com.raposo.habittracker.application.report;

import java.util.List;

import com.raposo.habittracker.domain.DateRange;

public record HabitReport(
                ReportContext context,
                DateRange currentRange,
                DateRange previousRange,
                List<EntryReportRow> entries,
                List<HabitSummaryRow> summary) {
}
