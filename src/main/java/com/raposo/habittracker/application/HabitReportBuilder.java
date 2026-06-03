package com.raposo.habittracker.application;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.raposo.habittracker.application.report.EntryReportRow;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.application.report.HabitSummaryRow;
import com.raposo.habittracker.application.report.ReportContext;
import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.StoredEntry;

public class HabitReportBuilder {

        public HabitReport build(Map<EntryKey, StoredEntry> entries, DateRange range) {
                ReportContext context = new ReportContext(
                                "0 = bad, 1 = weak, 2 = acceptable, 3 = good",
                                "Week starts on Monday and ends on Sunday",
                                "Missing entry means no stored data; summary averages treat it as 0");

                List<EntryReportRow> entryRows = entries.entrySet().stream()
                                .sorted(Comparator
                                                .comparing((Map.Entry<EntryKey, StoredEntry> entry) -> entry.getKey()
                                                                .entryDate())
                                                .thenComparing(entry -> entry.getKey().habit()))
                                .map(entry -> toEntryReportRow(entry.getKey(), entry.getValue()))
                                .toList();

                List<HabitSummaryRow> summaryRows = buildSummaryRows(entries, range);

                return new HabitReport(context, entryRows, summaryRows);
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
                        Map<EntryKey, StoredEntry> entries,
                        DateRange range) {
                long totalDays = range.daysInclusive();

                return entries.keySet().stream()
                                .map(EntryKey::habit)
                                .distinct()
                                .sorted()
                                .map(habit -> toHabitSummaryRow(habit, entries, totalDays))
                                .toList();
        }

        private HabitSummaryRow toHabitSummaryRow(
                        String habit,
                        Map<EntryKey, StoredEntry> entries,
                        long totalDays) {
                List<StoredEntry> habitEntries = entries.entrySet().stream()
                                .filter(entry -> entry.getKey().habit().equals(habit))
                                .map(Map.Entry::getValue)
                                .toList();

                double totalScore = habitEntries.stream()
                                .mapToDouble(StoredEntry::score)
                                .sum();

                int recordedDays = habitEntries.size();
                int missingDays = (int) totalDays - recordedDays;

                double averageScore = totalScore / totalDays;

                return new HabitSummaryRow(
                                habit,
                                averageScore,
                                recordedDays,
                                missingDays);
        }

}
