package com.raposo.habittracker.application;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.raposo.habittracker.application.report.EntryReportRow;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.application.report.HabitSummaryRow;
import com.raposo.habittracker.application.report.ReportContext;
import com.raposo.habittracker.application.report.Trend;
import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.StoredEntry;

public class HabitReportBuilder {

        public HabitReport build(
                        Map<EntryKey, StoredEntry> currentEntries,
                        DateRange currentRange,
                        Map<EntryKey, StoredEntry> previousEntries,
                        DateRange previousRange,
                        Optional<LocalDate> trackingStartDate) {
                ReportContext context = new ReportContext(
                                "0 = bad, 1 = weak, 2 = acceptable, 3 = good",
                                "Week starts on Monday and ends on Sunday",
                                "Missing entry means no stored data; period score treats it as 0");

                List<EntryReportRow> entryRows = currentEntries.entrySet().stream()
                                .sorted(Comparator
                                                .comparing((Map.Entry<EntryKey, StoredEntry> entry) -> entry.getKey()
                                                                .entryDate())
                                                .thenComparing(entry -> entry.getKey().habit()))
                                .map(entry -> toEntryReportRow(entry.getKey(), entry.getValue()))
                                .toList();

                List<HabitSummaryRow> summaryRows = buildSummaryRows(
                                currentEntries,
                                currentRange,
                                previousEntries,
                                previousRange);
                return new HabitReport(context,
                                currentRange,
                                previousRange,
                                entryRows,
                                summaryRows);
        }

        private EntryReportRow toEntryReportRow(EntryKey key, StoredEntry entry) {
                LocalDate entryDate = key.entryDate();

                String weekday = entryDate.getDayOfWeek()
                                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                LocalDate weekStart = entryDate.with(
                                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

                return new EntryReportRow(
                                entryDate,
                                weekday,
                                weekStart,
                                key.habit(),
                                entry.score(),
                                entry.note());
        }

        private List<HabitSummaryRow> buildSummaryRows(
                        Map<EntryKey, StoredEntry> currentEntries,
                        DateRange currentRange,
                        Map<EntryKey, StoredEntry> previousEntries,
                        DateRange previousRange) {
                return Stream.concat(
                                currentEntries.keySet().stream(),
                                previousEntries.keySet().stream())
                                .map(EntryKey::habit)
                                .distinct()
                                .sorted()
                                .map(habit -> toHabitSummaryRow(
                                                habit,
                                                currentEntries,
                                                currentRange,
                                                previousEntries,
                                                previousRange))
                                .toList();
        }

        private HabitSummaryRow toHabitSummaryRow(
                        String habit,
                        Map<EntryKey, StoredEntry> currentEntries,
                        DateRange currentRange,
                        Map<EntryKey, StoredEntry> previousEntries,
                        DateRange previousRange) {
                double previousPeriodScore = periodScoreForHabit(
                                habit,
                                previousEntries,
                                previousRange);

                double currentPeriodScore = periodScoreForHabit(
                                habit,
                                currentEntries,
                                currentRange);

                int previousRecordedDays = recordedDaysForHabit(habit, previousEntries);
                int previousMissingDays = (int) previousRange.daysInclusive() - previousRecordedDays;

                int currentRecordedDays = recordedDaysForHabit(habit, currentEntries);
                int currentMissingDays = (int) currentRange.daysInclusive() - currentRecordedDays;

                double delta = previousRecordedDays == 0
                                ? 0
                                : currentPeriodScore - previousPeriodScore;

                return new HabitSummaryRow(
                                habit,
                                previousPeriodScore,
                                currentPeriodScore,
                                delta,
                                trendFrom(delta, previousRecordedDays),
                                previousRecordedDays,
                                previousMissingDays,
                                currentRecordedDays,
                                currentMissingDays);
        }

        private double periodScoreForHabit(
                        String habit,
                        Map<EntryKey, StoredEntry> entries,
                        DateRange range) {
                double totalScore = entries.entrySet().stream()
                                .filter(entry -> entry.getKey().habit().equals(habit))
                                .mapToDouble(entry -> entry.getValue().score())
                                .sum();

                return totalScore / range.daysInclusive();
        }

        private int recordedDaysForHabit(
                        String habit,
                        Map<EntryKey, StoredEntry> entries) {
                return (int) entries.keySet().stream()
                                .filter(key -> key.habit().equals(habit))
                                .count();
        }

        private Trend trendFrom(double delta, int previousRecordedDays) {
                if (previousRecordedDays == 0) {
                        return Trend.NO_BASELINE;
                }

                if (delta > 0) {
                        return Trend.IMPROVED;
                }

                if (delta < 0) {
                        return Trend.WORSENED;
                }

                return Trend.STABLE;
        }
}
