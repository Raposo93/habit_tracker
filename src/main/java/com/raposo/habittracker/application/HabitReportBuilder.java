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
                                previousRange,
                                trackingStartDate);
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
                        DateRange previousRange,
                        Optional<LocalDate> trackingStartDate) {
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
                                                previousRange,
                                                trackingStartDate))
                                .toList();
        }

        private HabitSummaryRow toHabitSummaryRow(
                        String habit,
                        Map<EntryKey, StoredEntry> currentEntries,
                        DateRange currentRange,
                        Map<EntryKey, StoredEntry> previousEntries,
                        DateRange previousRange,
                        Optional<LocalDate> trackingStartDate) {

                int previousEvaluableDays = evaluableDays(previousRange, trackingStartDate);
                int currentEvaluableDays = evaluableDays(currentRange, trackingStartDate);

                double previousPeriodScore = periodScoreForHabit(
                                habit,
                                previousEntries,
                                trackingStartDate,
                                previousEvaluableDays);

                double currentPeriodScore = periodScoreForHabit(
                                habit,
                                currentEntries,
                                trackingStartDate,
                                currentEvaluableDays);

                int previousRecordedDays = recordedDaysForHabit(
                                habit,
                                previousEntries,
                                trackingStartDate);
                int previousMissingDays = previousEvaluableDays - previousRecordedDays;

                int currentRecordedDays = recordedDaysForHabit(
                                habit,
                                currentEntries,
                                trackingStartDate);
                int currentMissingDays = currentEvaluableDays - currentRecordedDays;

                boolean hasBaseline = trackingStartDate
                                .map(startDate -> previousEvaluableDays > 0)
                                .orElse(previousRecordedDays > 0);

                double delta = hasBaseline
                                ? currentPeriodScore - previousPeriodScore
                                : 0;

                return new HabitSummaryRow(
                                habit,
                                previousPeriodScore,
                                currentPeriodScore,
                                delta,
                                trendFrom(delta, hasBaseline),
                                previousRecordedDays,
                                previousMissingDays,
                                currentRecordedDays,
                                currentMissingDays);
        }

        private double periodScoreForHabit(
                        String habit,
                        Map<EntryKey, StoredEntry> entries,
                        Optional<LocalDate> trackingStartDate,
                        int evaluableDays) {
                if (evaluableDays == 0) {
                        return 0;
                }

                double totalScore = entries.entrySet().stream()
                                .filter(entry -> entry.getKey().habit().equals(habit))
                                .filter(entry -> isEvaluableDate(entry.getKey().entryDate(), trackingStartDate))
                                .mapToDouble(entry -> entry.getValue().score())
                                .sum();

                return totalScore / evaluableDays;
        }

        private int recordedDaysForHabit(
                        String habit,
                        Map<EntryKey, StoredEntry> entries,
                        Optional<LocalDate> trackingStartDate) {

                return (int) entries.keySet().stream()
                                .filter(key -> key.habit().equals(habit))
                                .filter(key -> isEvaluableDate(key.entryDate(), trackingStartDate))
                                .count();
        }

        private Trend trendFrom(double delta, boolean hasBaseline) {
                if (!hasBaseline) {
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

        private int evaluableDays(
                        DateRange range,
                        Optional<LocalDate> trackingStartDate) {

                LocalDate effectiveStartDate = trackingStartDate
                                .filter(startDate -> startDate.isAfter(range.startDate()))
                                .orElse(range.startDate());

                if (effectiveStartDate.isAfter(range.endDate())) {
                        return 0;
                }

                return (int) DateRange.of(effectiveStartDate, range.endDate())
                                .daysInclusive();
        }

        private boolean isEvaluableDate(
                        LocalDate date,
                        Optional<LocalDate> trackingStartDate) {

                return trackingStartDate
                                .map(startDate -> !date.isBefore(startDate))
                                .orElse(true);
        }
}
