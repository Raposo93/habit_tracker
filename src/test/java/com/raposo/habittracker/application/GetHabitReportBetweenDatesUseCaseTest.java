package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.application.report.HabitSummaryRow;
import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.HabitEntry;
import com.raposo.habittracker.domain.StoredEntry;

class GetHabitReportBetweenDatesUseCaseTest {

    @Test
    void givenDateRangeWhenExecuteThenQueryCurrentAndPreviousRanges() {
        DateRange currentRange = DateRange.of(
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 31));

        DateRange previousRange = DateRange.of(
                LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 5, 24));

        SpyHabitEntryRepository repository = new SpyHabitEntryRepository();

        GetHabitReportBetweenDatesUseCase useCase = new GetHabitReportBetweenDatesUseCase(repository);

        useCase.execute(currentRange);

        assertEquals(List.of(currentRange, previousRange), repository.queriedRanges());
    }

    @Test
    void givenTrackingStartDateWhenExecuteThenIgnorePreviousDaysBeforeTrackingStarted() {
        DateRange currentRange = DateRange.of(
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 31));

        DateRange previousRange = DateRange.of(
                LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 5, 24));

        SpyHabitEntryRepository repository = new SpyHabitEntryRepository();
        repository.earliestEntryDate = Optional.of(LocalDate.of(2026, 5, 22));
        repository.entriesByRange.put(
                previousRange,
                Map.of(
                        new EntryKey(LocalDate.of(2026, 5, 22), "Sleep"),
                        new StoredEntry(3.0, "")));
        repository.entriesByRange.put(
                currentRange,
                Map.of(
                        new EntryKey(LocalDate.of(2026, 5, 25), "Sleep"),
                        new StoredEntry(3.0, "")));

        GetHabitReportBetweenDatesUseCase useCase = new GetHabitReportBetweenDatesUseCase(repository);

        HabitReport report = useCase.execute(currentRange);

        HabitSummaryRow row = report.summary().stream()
                .filter(summaryRow -> summaryRow.habit().equals("Sleep"))
                .findFirst()
                .orElseThrow();

        assertEquals(1.0, row.previousPeriodScore(), 0.000001);
        assertEquals(1, row.previousRecordedDays());
        assertEquals(2, row.previousMissingDays());
    }

    private static class SpyHabitEntryRepository implements HabitEntryRepository {
        private final List<DateRange> queriedRanges = new ArrayList<>();
        private final Map<DateRange, Map<EntryKey, StoredEntry>> entriesByRange = new HashMap<>();
        private Optional<LocalDate> earliestEntryDate = Optional.empty();

        @Override
        public Map<EntryKey, StoredEntry> findEntriesBetweenDates(
                LocalDate startDate,
                LocalDate endDate) {

            DateRange range = DateRange.of(startDate, endDate);
            queriedRanges.add(range);

            return entriesByRange.getOrDefault(range, Map.of());
        }

        @Override
        public Optional<LocalDate> findEarliestEntryDate() {
            return earliestEntryDate;
        }

        @Override
        public Optional<LocalDate> findLatestEntryDate() {
            return Optional.empty();
        }

        @Override
        public void insertEntries(List<HabitEntry> entries) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateEntries(List<HabitEntry> entries) {
            throw new UnsupportedOperationException();
        }

        List<DateRange> queriedRanges() {
            return queriedRanges;
        }
    }
}
