package com.raposo.habittracker;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImportEntriesUseCaseTest {

    @Test
    void givenNoEntriesWhenExecuteThenRepositoryIsUnchanged() {
        FakeHabitEntryReader reader = new FakeHabitEntryReader(List.of());
        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();

        ImportEntriesUseCase useCase = new ImportEntriesUseCase(reader, repository);

        useCase.execute();

        assertEquals(0, repository.size());
    }

    @Test
    void givenNewEntriesWhenExecuteThenRepositoryContainsNewEntries() {
        HabitEntry entry = entry("2026-05-25", "Sleep", 8.0, "Good");

        FakeHabitEntryReader reader = new FakeHabitEntryReader(List.of(entry));
        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();

        ImportEntriesUseCase useCase = new ImportEntriesUseCase(reader, repository);

        useCase.execute();

        assertEquals(1, repository.size());
        assertEquals(new StoredEntry(8.0, "Good"), repository.get(entry));
    }

    @Test
    void givenExistingEntryWithDifferentScoreWhenExecuteThenRepositoryContainsUpdatedEntry() {
        HabitEntry oldEntry = entry("2026-05-25", "Sleep", 6.0, "Good");
        HabitEntry newEntry = entry("2026-05-25", "Sleep", 8.0, "Good");

        FakeHabitEntryReader reader = new FakeHabitEntryReader(List.of(newEntry));
        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();
        repository.saveExisting(oldEntry);

        ImportEntriesUseCase useCase = new ImportEntriesUseCase(reader, repository);

        useCase.execute();

        assertEquals(1, repository.size());
        assertEquals(new StoredEntry(8.0, "Good"), repository.get(newEntry));
    }

    @Test
    void givenExistingEntryWithDifferentNoteWhenExecuteThenRepositoryContainsUpdatedEntry() {
        HabitEntry oldEntry = entry("2026-05-25", "Sleep", 6.0, "Good");
        HabitEntry newEntry = entry("2026-05-25", "Sleep", 6.0, "Better");

        FakeHabitEntryReader reader = new FakeHabitEntryReader(List.of(newEntry));
        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();
        repository.saveExisting(oldEntry);

        ImportEntriesUseCase useCase = new ImportEntriesUseCase(reader, repository);

        useCase.execute();

        assertEquals(1, repository.size());
        assertEquals(new StoredEntry(6.0, "Better"), repository.get(newEntry));
    }

    @Test
    void givenExistingEqualEntryWhenExecuteThenRepositoryKeepsSameEntry() {
        HabitEntry entry = entry("2026-05-25", "Sleep", 8.0, "Good");

        FakeHabitEntryReader reader = new FakeHabitEntryReader(List.of(entry));
        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();
        repository.saveExisting(entry);

        ImportEntriesUseCase useCase = new ImportEntriesUseCase(reader, repository);

        useCase.execute();

        assertEquals(1, repository.size());
        assertEquals(new StoredEntry(8.0, "Good"), repository.get(entry));
    }

    @Test
    void givenOlderMissingEntryWhenExecuteThenRepositoryDoesNotContainOlderEntry() {
        HabitEntry existingEntry = entry("2026-05-26", "Sleep", 8.0, "Good");
        HabitEntry oldEntry = entry("2026-05-20", "Sleep", 6.0, "Good");

        FakeHabitEntryReader reader = new FakeHabitEntryReader(List.of(existingEntry));
        InMemoryHabitEntryRepository repository = new InMemoryHabitEntryRepository();
        repository.saveExisting(existingEntry);

        ImportEntriesUseCase useCase = new ImportEntriesUseCase(reader, repository);

        useCase.execute();

        assertEquals(1, repository.size());
        assertEquals(new StoredEntry(8.0, "Good"), repository.get(existingEntry));
        assertNull(repository.get(oldEntry));
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

        boolean contains(HabitEntry entry) {
            return entries.containsKey(new EntryKey(entry.entryDate(), entry.habit()));
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
}
