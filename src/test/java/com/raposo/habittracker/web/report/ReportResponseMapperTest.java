package com.raposo.habittracker.web.report;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.report.EntryReportRow;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.application.report.HabitSummaryRow;
import com.raposo.habittracker.application.report.ReportContext;
import com.raposo.habittracker.application.report.Trend;
import com.raposo.habittracker.domain.DateRange;

class ReportResponseMapperTest {

    @Test
    void givenHabitReportWithNoBaselineWhenToResponseThenPreviousPeriodScoreIsNull() {
        HabitReport report = reportWithNoBaseline();

        ReportResponseMapper mapper = new ReportResponseMapper();

        ReportResponse response = mapper.toResponse(report);

        assertNull(response.summary().getFirst().previousPeriodScore());

    }

    private HabitReport reportWithNoBaseline() {

        ReportContext context = new ReportContext(
                "0 = bad, 1 = weak, 2 = acceptable, 3 = good",
                "Week starts on Monday and ends on Sunday",
                "Missing entry means no stored data; period score treats it as 0");

        DateRange currentRange = DateRange.of(
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 31));
        DateRange previousRange = DateRange.of(
                LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 5, 24));
        List<EntryReportRow> entries = List.of();

        String habit = "habit";
        double previousPeriodScore = 0.0;
        double currentPeriodScore = 0.0;
        double delta = 0.0;
        Trend trend = Trend.NO_BASELINE;
        int previousRecordedDays = 0;
        int previousMissingDays = 7;
        int currentRecordedDays = 7;
        int currentMissingDays = 0;

        HabitSummaryRow summary = new HabitSummaryRow(
                habit,
                previousPeriodScore,
                currentPeriodScore,
                delta,
                trend,
                previousRecordedDays,
                previousMissingDays,
                currentRecordedDays,
                currentMissingDays);

        return new HabitReport(
                context,
                currentRange,
                previousRange,
                entries,
                List.of(summary));
    }
}
