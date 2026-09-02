package com.raposo.habittracker.application;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.port.HabitEntryReader;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitEntry;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;
import java.util.Optional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImportEntriesUseCaseTest {

    @Test
    void givenNoEntriesWhenExecuteThenRepositoryIsUnchanged() {
        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();

        ImportEntriesUseCase useCase = createUseCase(
                List.of(),
                repository);

        useCase.execute();

        assertEquals(0, repository.size());
    }

    @Test
    void givenNewEntriesWhenExecuteThenRepositoryContainsNewEntries() {
        HabitEntry entry = entry("2026-05-25", "Sleep", 8.0, "Good");

        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();

        ImportEntriesUseCase useCase = createUseCase(
                List.of(entry),
                repository);

        useCase.execute();

        assertEquals(1, repository.size());
        assertEquals(new StoredEntry(8.0, "Good"), repository.get(entry));
    }

    @Test
    void givenExistingEntryWithDifferentScoreWhenExecuteThenRepositoryContainsUpdatedEntry() {
        HabitEntry oldEntry = entry("2026-05-25", "Sleep", 6.0, "Good");
        HabitEntry newEntry = entry("2026-05-25", "Sleep", 8.0, "Good");

        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();
        repository.saveExisting(oldEntry);

        ImportEntriesUseCase useCase = createUseCase(
                List.of(newEntry),
                repository);

        useCase.execute();

        assertEquals(1, repository.size());
        assertEquals(new StoredEntry(8.0, "Good"), repository.get(newEntry));
    }

    @Test
    void givenExistingEntryWithDifferentNoteWhenExecuteThenRepositoryContainsUpdatedEntry() {
        HabitEntry oldEntry = entry("2026-05-25", "Sleep", 6.0, "Good");
        HabitEntry newEntry = entry("2026-05-25", "Sleep", 6.0, "Better");

        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();
        repository.saveExisting(oldEntry);

        ImportEntriesUseCase useCase = createUseCase(
                List.of(newEntry),
                repository);

        useCase.execute();

        assertEquals(1, repository.size());
        assertEquals(new StoredEntry(6.0, "Better"), repository.get(newEntry));
    }

    @Test
    void givenExistingEqualEntryWhenExecuteThenRepositoryKeepsSameEntry() {
        HabitEntry entry = entry("2026-05-25", "Sleep", 8.0, "Good");

        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();
        repository.saveExisting(entry);

        ImportEntriesUseCase useCase = createUseCase(
                List.of(entry),
                repository);

        useCase.execute();

        assertEquals(1, repository.size());
        assertEquals(new StoredEntry(8.0, "Good"), repository.get(entry));
    }

    @Test
    void givenOlderMissingEntryWhenExecuteThenRepositoryDoesNotContainOlderEntry() {
        HabitEntry existingEntry = entry("2026-05-26", "Sleep", 8.0, "Good");
        HabitEntry oldEntry = entry("2026-05-20", "Sleep", 6.0, "Good");

        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();
        repository.saveExisting(existingEntry);

        ImportEntriesUseCase useCase = createUseCase(
                List.of(oldEntry),
                repository);

        useCase.execute();

        assertEquals(1, repository.size());
        assertEquals(new StoredEntry(8.0, "Good"), repository.get(existingEntry));
        assertNull(repository.get(oldEntry));
    }

    @Test
    void givenUnknownSheetHabitWhenExecuteThenThrowClearException() {
        HabitEntry entry = entry("2026-06-08", "Unknown habit", 2.0, "");

        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();

        ImportEntriesUseCase useCase = new ImportEntriesUseCase(
                new FakeHabitEntryReader(List.of(entry)),
                repository,
                new InMemoryHabitRepository(List.of()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                useCase::execute);

        assertEquals(
                "Unknown habit from Sheets: Unknown habit",
                exception.getMessage());

        assertEquals(0, repository.size());
    }

    @Test
    void givenTwoKnownHabitsOnSameDateWhenExecuteThenRepositoryContainsBothEntries() {
        HabitEntry sleep = entry("2026-06-08", "Sleep", 2.0, "");
        HabitEntry exercise = entry("2026-06-08", "Exercise", 3.0, "");

        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();

        ImportEntriesUseCase useCase = createUseCase(
                List.of(sleep, exercise),
                repository,
                List.of("Sleep", "Exercise"));

        useCase.execute();

        assertEquals(2, repository.size());
        assertEquals(new StoredEntry(2.0, ""), repository.get(sleep));
        assertEquals(new StoredEntry(3.0, ""), repository.get(exercise));
    }

    private static ImportEntriesUseCase createUseCase(
            List<HabitEntry> entries,
            InMemoryHabitEntryRepository repository,
            List<String> knownHabits) {
        return new ImportEntriesUseCase(
                new FakeHabitEntryReader(entries),
                repository,
                new InMemoryHabitRepository(knownHabits));
    }

    private static HabitEntry entry(String date, String habit, double score, String note) {
        return new HabitEntry(
                LocalDate.parse(date),
                habit,
                score,
                note);
    }

    private static class InMemoryHabitEntryRepository implements HabitEntryRepository {
        private final Map<EntryKey, StoredEntry> entries = new HashMap<>();

        void saveExisting(HabitEntry entry) {
            entries.put(
                    new EntryKey(entry.entryDate(), entry.habit()),
                    new StoredEntry(entry.score(), entry.note()));
        }

        StoredEntry get(HabitEntry entry) {
            return entries.get(new EntryKey(entry.entryDate(), entry.habit()));
        }

        int size() {
            return entries.size();
        }

        @Override
        public Optional<LocalDate> findLatestEntryDate() {
            return entries.keySet().stream()
                    .map(EntryKey::entryDate)
                    .max(LocalDate::compareTo);
        }

        @Override
        public Map<EntryKey, StoredEntry> findEntriesBetweenDates(
                LocalDate startDate,
                LocalDate endDate) {
            return entries.entrySet().stream()
                    .filter(entry -> !entry.getKey().entryDate().isBefore(startDate)
                            && !entry.getKey().entryDate().isAfter(endDate))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue));
        }

        @Override
        public Map<HabitId, StoredEntry> findEntriesByDate(LocalDate date) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insertEntries(List<HabitEntry> entries) {
            for (HabitEntry entry : entries) {
                saveExisting(entry);
            }
        }

        @Override
        public void updateEntries(List<HabitEntry> entries) {
            for (HabitEntry entry : entries) {
                saveExisting(entry);
            }
        }

        @Override
        public Optional<LocalDate> findEarliestEntryDate() {
            return entries.keySet().stream()
                    .map(EntryKey::entryDate)
                    .min(LocalDate::compareTo);
        }
    }

    private static ImportEntriesUseCase createUseCase(
            List<HabitEntry> entries,
            InMemoryHabitEntryRepository repository) {
        return new ImportEntriesUseCase(
                new FakeHabitEntryReader(entries),
                repository,
                new InMemoryHabitRepository(habitNamesFrom(entries)));
    }

    private static List<String> habitNamesFrom(List<HabitEntry> entries) {
        return entries.stream()
                .map(HabitEntry::habit)
                .distinct()
                .toList();
    }

    private static class FakeHabitEntryReader implements HabitEntryReader {
        private final List<HabitEntry> entries;

        private FakeHabitEntryReader(List<HabitEntry> entries) {
            this.entries = entries;
        }

        @Override
        public List<HabitEntry> readEntries() {
            return entries;
        }
    }

    private static class InMemoryHabitRepository implements HabitRepository {
        private final List<String> habitNames;

        InMemoryHabitRepository(List<String> habitNames) {
            this.habitNames = habitNames;
        }

        @Override
        public Optional<Habit> findByExactName(String name) {
            return habitNames.stream()
                    .filter(habitName -> habitName.equals(name))
                    .findFirst()
                    .map(this::habit);
        }

        @Override
        public Optional<Habit> findById(HabitId id) {
            return Optional.empty();
        }

        @Override
        public List<Habit> findActive() {
            return habitNames.stream()
                    .map(this::habit)
                    .toList();
        }

        @Override
        public List<Habit> findAll() {
            return findActive();
        }

        private Habit habit(String name) {
            return new Habit(
                    HabitId.of("habit-" + name),
                    name,
                    HabitCadence.DAILY,
                    true);
        }
    }
}
