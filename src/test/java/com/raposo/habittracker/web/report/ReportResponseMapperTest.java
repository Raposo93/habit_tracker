package com.raposo.habittracker.web.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void givenHabitReportWithNoBaselineWhenToResponseThenDeltaIsNull() {
        HabitReport report = reportWithNoBaseline();

        ReportResponseMapper mapper = new ReportResponseMapper();

        ReportResponse response = mapper.toResponse(report);

        assertNull(response.summary().getFirst().delta());
    }

    @Test
    void givenHabitReportWithNoBaselineWhenToResponseThenTrendIsNO_BASELINE() {
        HabitReport report = reportWithNoBaseline();

        ReportResponseMapper mapper = new ReportResponseMapper();

        ReportResponse response = mapper.toResponse(report);

        assertEquals(Trend.NO_BASELINE.name(), response.summary().getFirst().trend());
    }

    @Test
    void givenHabitReportWithNoBaselineWhenToResponseThenRecordedAndMissingDaysArePreserved() {
        int previousRecordedDays = 2;
        int previousMissingDays = 5;
        int currentRecordedDays = 3;
        int currentMissingDays = 4;

        HabitReport report = reportWithNoBaseline(
                previousRecordedDays,
                previousMissingDays,
                currentRecordedDays,
                currentMissingDays);

        ReportResponseMapper mapper = new ReportResponseMapper();

        ReportResponse response = mapper.toResponse(report);

        ReportResponse.HabitSummaryResponse summary = response.summary().getFirst();

        assertEquals(previousRecordedDays, summary.previousRecordedDays());
        assertEquals(previousMissingDays, summary.previousMissingDays());
        assertEquals(currentRecordedDays, summary.currentRecordedDays());
        assertEquals(currentMissingDays, summary.currentMissingDays());
    }

    private HabitReport reportWithNoBaseline(
            int previousRecordedDays,
            int previousMissingDays,
            int currentRecordedDays,
            int currentMissingDays) {
        ReportContext context = defaultContext();

        DateRange currentRange = DateRange.of(
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 31));
        DateRange previousRange = DateRange.of(
                LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 5, 24));
        List<EntryReportRow> entries = List.of();

        HabitSummaryRow summary = noBaselineSummary(
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

    private HabitReport reportWithNoBaseline() {
        return reportWithNoBaseline(0, 7, 0, 7);
    }

    private ReportContext defaultContext() {
        return new ReportContext(
                "0 = bad, 1 = weak, 2 = acceptable, 3 = good",
                "Week starts on Monday and ends on Sunday",
                "Missing entry means no stored data; period score treats it as 0");
    }

    private HabitSummaryRow noBaselineSummary(
            int previousRecordedDays,
            int previousMissingDays,
            int currentRecordedDays,
            int currentMissingDays) {
        String habit = "habit";
        double previousPeriodScore = 0.0;
        double currentPeriodScore = 0.0;
        double delta = 0.0;
        Trend trend = Trend.NO_BASELINE;

        return new HabitSummaryRow(
                habit,
                previousPeriodScore,
                currentPeriodScore,
                delta,
                trend,
                previousRecordedDays,
                previousMissingDays,
                currentRecordedDays,
                currentMissingDays);
    }
}
