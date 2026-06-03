package com.raposo.habittracker.application.report;

import java.util.List;

public record HabitReport(
        ReportContext context,
        List<EntryReportRow> entries,
        List<HabitSummaryRow> summary
) {
}
