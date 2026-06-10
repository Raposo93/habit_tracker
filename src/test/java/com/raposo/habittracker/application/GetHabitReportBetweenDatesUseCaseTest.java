package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.port.HabitEntryRepository;
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

        GetHabitReportBetweenDatesUseCase useCase =
                new GetHabitReportBetweenDatesUseCase(repository);

        useCase.execute(currentRange);

        assertEquals(List.of(currentRange, previousRange), repository.queriedRanges());
    }

    private static class SpyHabitEntryRepository implements HabitEntryRepository {

        private final List<DateRange> queriedRanges = new ArrayList<>();

        @Override
        public Optional<LocalDate> findLatestEntryDate() {
            throw new UnsupportedOperationException("Not needed in this test");
        }

        @Override
        public Map<EntryKey, StoredEntry> findEntriesBetweenDates(
                LocalDate startDate,
                LocalDate endDate) {
            queriedRanges.add(DateRange.of(startDate, endDate));
            return Map.of();
        }

        @Override
        public void insertEntries(List<HabitEntry> entries) {
            throw new UnsupportedOperationException("Not needed in this test");
        }

        @Override
        public void updateEntries(List<HabitEntry> entries) {
            throw new UnsupportedOperationException("Not needed in this test");
        }

        List<DateRange> queriedRanges() {
            return queriedRanges;
        }

        @Override
        public Optional<LocalDate> findEarliestEntryDate() {
            return Optional.empty();
        }
    }
}
