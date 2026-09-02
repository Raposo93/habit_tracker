package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.entry.DailyEntryContext;
import com.raposo.habittracker.application.entry.HabitEntryContext;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitEntry;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;

class GetDailyEntryContextUseCaseTest {

    @Test
    void givenSelectedDateWhenExecuteThenReturnActiveHabitsWithEntriesMatchedByHabitId() {
        LocalDate date = LocalDate.of(2026, 9, 2);
        HabitId sleepId = HabitId.of("sleep");
        HabitId exerciseId = HabitId.of("exercise");

        FakeHabitRepository habitRepository = new FakeHabitRepository(List.of(
                Habit.active(sleepId, "Rest", HabitCadence.DAILY),
                Habit.active(exerciseId, "Exercise", HabitCadence.DAILY),
                Habit.inactive(HabitId.of("review"), "Review", HabitCadence.DAILY)));

        FakeHabitEntryRepository entryRepository = new FakeHabitEntryRepository(
                Map.of(sleepId, new StoredEntry(0.0, "Tired")));

        GetDailyEntryContextUseCase useCase = new GetDailyEntryContextUseCase(
                habitRepository,
                entryRepository);

        DailyEntryContext result = useCase.execute(date);

        assertEquals(date, result.date());
        assertEquals(date, entryRepository.queriedDate);
        assertEquals(
                List.of(
                        new HabitEntryContext(
                                sleepId,
                                "Rest",
                                Optional.of(new StoredEntry(0.0, "Tired"))),
                        new HabitEntryContext(
                                exerciseId,
                                "Exercise",
                                Optional.empty())),
                result.habits());
    }

    private static class FakeHabitRepository implements HabitRepository {
        private final List<Habit> habits;

        private FakeHabitRepository(List<Habit> habits) {
            this.habits = habits;
        }

        @Override
        public Optional<Habit> findById(HabitId id) {
            return habits.stream()
                    .filter(habit -> habit.id().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<Habit> findByExactName(String name) {
            return habits.stream()
                    .filter(habit -> habit.name().equals(name))
                    .findFirst();
        }

        @Override
        public List<Habit> findActive() {
            return habits.stream()
                    .filter(Habit::active)
                    .toList();
        }

        @Override
        public List<Habit> findAll() {
            return habits;
        }
    }

    private static class FakeHabitEntryRepository implements HabitEntryRepository {
        private final Map<HabitId, StoredEntry> entries;
        private LocalDate queriedDate;

        private FakeHabitEntryRepository(Map<HabitId, StoredEntry> entries) {
            this.entries = entries;
        }

        @Override
        public Map<HabitId, StoredEntry> findEntriesByDate(LocalDate date) {
            queriedDate = date;
            return entries;
        }

        @Override
        public Optional<StoredEntry> findEntry(LocalDate date, HabitId habitId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean createEntry(LocalDate date, HabitId habitId, StoredEntry entry) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean updateEntry(LocalDate date, HabitId habitId, StoredEntry entry) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<LocalDate> findLatestEntryDate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<EntryKey, StoredEntry> findEntriesBetweenDates(
                LocalDate startDate,
                LocalDate endDate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insertEntries(List<HabitEntry> entries) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateEntries(List<HabitEntry> entries) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<LocalDate> findEarliestEntryDate() {
            throw new UnsupportedOperationException();
        }
    }
}
