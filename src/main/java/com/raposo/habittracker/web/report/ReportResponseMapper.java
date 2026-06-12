package com.raposo.habittracker.web.report;

import com.raposo.habittracker.application.report.EntryReportRow;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.application.report.HabitSummaryRow;
import com.raposo.habittracker.application.report.Trend;
import com.raposo.habittracker.domain.DateRange;

public class ReportResponseMapper {

    public ReportResponse toResponse(HabitReport report) {
        return new ReportResponse(
                toContextResponse(report),
                toRangeResponse(report.currentRange()),
                toRangeResponse(report.previousRange()),
                report.summary().stream()
                        .map(this::toSummaryResponse)
                        .toList(),
                report.entries().stream()
                        .map(this::toEntryResponse)
                        .toList());
    }

    private ReportResponse.ReportContextResponse toContextResponse(HabitReport report) {
        return new ReportResponse.ReportContextResponse(
                report.context().scoreScale());
    }

    private ReportResponse.ReportRangeResponse toRangeResponse(DateRange range) {
        return new ReportResponse.ReportRangeResponse(
                range.startDate(),
                range.endDate());
    }

    private ReportResponse.HabitSummaryResponse toSummaryResponse(HabitSummaryRow row) {
        boolean noBaseline = row.trend() == Trend.NO_BASELINE;

        return new ReportResponse.HabitSummaryResponse(
                row.habit(),
                noBaseline ? null : row.previousPeriodScore(),
                row.currentPeriodScore(),
                noBaseline ? null : row.delta(),
                row.trend().name(),
                row.previousRecordedDays(),
                row.previousMissingDays(),
                row.currentRecordedDays(),
                row.currentMissingDays());
    }

    private ReportResponse.EntryResponse toEntryResponse(EntryReportRow row) {
        return new ReportResponse.EntryResponse(
                row.date(),
                row.habit(),
                row.score(),
                row.note());
    }
}