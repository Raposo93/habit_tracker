package com.raposo.habittracker.web.report;

import java.time.LocalDate;
import java.util.List;

public record ReportResponse(
        ReportContextResponse context,
        ReportRangeResponse currentRange,
        ReportRangeResponse previousRange,
        List<HabitSummaryResponse> summary,
        List<EntryResponse> entries,
        List<ScoringGuideResponse> scoringGuides) {
    public ReportResponse(ReportContextResponse context, ReportRangeResponse currentRange,
            ReportRangeResponse previousRange, List<HabitSummaryResponse> summary, List<EntryResponse> entries) {
        this(context, currentRange, previousRange, summary, entries, List.of());
    }

    public record ScoringGuideResponse(String habitId, String habitName, String scoringGuide) {
    }

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
