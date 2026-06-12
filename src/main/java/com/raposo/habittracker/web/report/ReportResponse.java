package com.raposo.habittracker.web.report;

import java.time.LocalDate;
import java.util.List;

public record ReportResponse(
        ReportContextResponse context,
        ReportRangeResponse currentRange,
        ReportRangeResponse previousRange,
        List<HabitSummaryResponse> summary,
        List<EntryResponse> entries) {

    public record ReportContextResponse(
            String scoreScale) {
    }

    public record ReportRangeResponse(
            LocalDate start,
            LocalDate end) {
    }

    public record HabitSummaryResponse(
            String habit,
            Double previousPeriodScore,
            Double currentPeriodScore,
            Double delta,
            String trend,
            int previousRecordedDays,
            int previousMissingDays,
            int currentRecordedDays,
            int currentMissingDays) {
    }

    public record EntryResponse(
            LocalDate date,
            String habit,
            double score,
            String note) {
    }
}
