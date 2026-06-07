package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.application.report.HabitSummaryRow;
import com.raposo.habittracker.application.report.Trend;
import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.StoredEntry;

class HabitReportBuilderTest {

    private static final double ASSERTION_DELTA = 0.000001;

    private final HabitReportBuilder builder = new HabitReportBuilder();

    @Test
    void givenCurrentEntriesWhenBuildThenCalculateCurrentSummaryWithMissingDays() {
        DateRange currentRange = weekStarting(2026, 5, 25);
        DateRange previousRange = weekStarting(2026, 5, 18);

        Map<EntryKey, StoredEntry> currentEntries = Map.of(
                key("Sleep", 2026, 5, 25), entry(3.0),
                key("Sleep", 2026, 5, 26), entry(1.0));

        HabitReport report = builder.build(
                currentEntries,
                currentRange,
                Map.of(),
                previousRange);

        HabitSummaryRow row = summaryRowFor(report, "Sleep");

        assertEquals("Sleep", row.habit());
        assertEquals(0.0, row.previousPeriodScore(), ASSERTION_DELTA);
        assertEquals(4.0 / 7.0, row.currentPeriodScore(), ASSERTION_DELTA);
        assertEquals(0.0, row.delta(), ASSERTION_DELTA);
        assertEquals(Trend.NO_BASELINE, row.trend());
        assertEquals(0, row.previousRecordedDays());
        assertEquals(7, row.previousMissingDays());
        assertEquals(2, row.currentRecordedDays());
        assertEquals(5, row.currentMissingDays());
    }

    @Test
    void givenCurrentAndPreviousEntriesWhenBuildThenCompareCurrentRangeWithPreviousRange() {
        DateRange currentRange = weekStarting(2026, 5, 25);
        DateRange previousRange = weekStarting(2026, 5, 18);

        Map<EntryKey, StoredEntry> currentEntries = Map.of(
                key("Exercise", 2026, 5, 25), entry(3.0),
                key("Exercise", 2026, 5, 26), entry(2.0));

        Map<EntryKey, StoredEntry> previousEntries = Map.of(
                key("Exercise", 2026, 5, 18), entry(1.0),
                key("Exercise", 2026, 5, 19), entry(1.0));

        HabitReport report = builder.build(
                currentEntries,
                currentRange,
                previousEntries,
                previousRange);

        HabitSummaryRow row = summaryRowFor(report, "Exercise");

        assertEquals(2.0 / 7.0, row.previousPeriodScore(), ASSERTION_DELTA);
        assertEquals(5.0 / 7.0, row.currentPeriodScore(), ASSERTION_DELTA);
        assertEquals(3.0 / 7.0, row.delta(), ASSERTION_DELTA);
        assertEquals(Trend.IMPROVED, row.trend());
        assertEquals(2, row.previousRecordedDays());
        assertEquals(5, row.previousMissingDays());
        assertEquals(2, row.currentRecordedDays());
        assertEquals(5, row.currentMissingDays());
    }

    @Test
    void givenCurrentPeriodScoreGreaterThanPreviousPeriodScoreWhenBuildThenMarkImprovedTrend() {
        DateRange currentRange = weekStarting(2026, 5, 25);
        DateRange previousRange = weekStarting(2026, 5, 18);

        HabitReport report = builder.build(
                Map.of(key("Reading", 2026, 5, 25), entry(3.0)),
                currentRange,
                Map.of(key("Reading", 2026, 5, 18), entry(1.0)),
                previousRange);

        HabitSummaryRow row = summaryRowFor(report, "Reading");

        assertEquals(Trend.IMPROVED, row.trend());
        assertEquals(2.0 / 7.0, row.delta(), ASSERTION_DELTA);
    }

    @Test
    void givenCurrentPeriodScoreLessThanPreviousPeriodScoreWhenBuildThenMarkWorsenedTrend() {
        DateRange currentRange = weekStarting(2026, 5, 25);
        DateRange previousRange = weekStarting(2026, 5, 18);

        HabitReport report = builder.build(
                Map.of(key("Reading", 2026, 5, 25), entry(1.0)),
                currentRange,
                Map.of(key("Reading", 2026, 5, 18), entry(3.0)),
                previousRange);

        HabitSummaryRow row = summaryRowFor(report, "Reading");

        assertEquals(Trend.WORSENED, row.trend());
        assertEquals(-2.0 / 7.0, row.delta(), ASSERTION_DELTA);
    }

    @Test
    void givenHabitOnlyExistsInPreviousRangeWhenBuildThenIncludeItInSummary() {
        DateRange currentRange = weekStarting(2026, 5, 25);
        DateRange previousRange = weekStarting(2026, 5, 18);

        HabitReport report = builder.build(
                Map.of(),
                currentRange,
                Map.of(key("Old habit", 2026, 5, 18), entry(3.0)),
                previousRange);

        HabitSummaryRow row = summaryRowFor(report, "Old habit");

        assertEquals("Old habit", row.habit());
        assertEquals(3.0 / 7.0, row.previousPeriodScore(), ASSERTION_DELTA);
        assertEquals(0.0, row.currentPeriodScore(), ASSERTION_DELTA);
        assertEquals(-3.0 / 7.0, row.delta(), ASSERTION_DELTA);
        assertEquals(Trend.WORSENED, row.trend());
        assertEquals(1, row.previousRecordedDays());
        assertEquals(6, row.previousMissingDays());
        assertEquals(0, row.currentRecordedDays());
        assertEquals(7, row.currentMissingDays());
    }

    private static HabitSummaryRow summaryRowFor(HabitReport report, String habit) {
        return report.summary().stream()
                .filter(row -> row.habit().equals(habit))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing summary row for habit: " + habit));
    }

    private static DateRange weekStarting(int year, int month, int day) {
        LocalDate start = LocalDate.of(year, month, day);
        return DateRange.of(start, start.plusDays(6));
    }

    private static EntryKey key(String habit, int year, int month, int day) {
        return new EntryKey(LocalDate.of(year, month, day), habit);
    }

    private static StoredEntry entry(double score) {
        return new StoredEntry(score, "");
    }
}
