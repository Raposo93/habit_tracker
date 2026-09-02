package com.raposo.habittracker.infrastructure.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;

class SqliteHabitEntryRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void givenEntriesWhenFindEarliestEntryDateThenReturnOldestDate() throws Exception {
        Path dbPath = tempDir.resolve("habit_tracker.db");
        SqliteHabitEntryRepository repository = new SqliteHabitEntryRepository(dbPath);

        insertEntry(dbPath, "sleep", "Sleep", "2026-06-10", 2.0);
        insertEntry(dbPath, "exercise", "Exercise", "2026-06-08", 3.0);
        insertEntry(dbPath, "review", "Review", "2026-06-09", 1.0);

        Optional<LocalDate> result = repository.findEarliestEntryDate();

        assertEquals(Optional.of(LocalDate.of(2026, 6, 8)), result);
    }

    @Test
    void givenNoEntriesWhenFindEarliestEntryDateThenReturnEmpty() {
        Path dbPath = tempDir.resolve("habit_tracker.db");
        SqliteHabitEntryRepository repository = new SqliteHabitEntryRepository(dbPath);

        Optional<LocalDate> result = repository.findEarliestEntryDate();

        assertTrue(result.isEmpty());
    }

    @Test
    void givenEntriesOnDifferentDatesWhenFindEntriesByDateThenReturnSelectedDateByHabitId() throws Exception {
        Path dbPath = tempDir.resolve("habit_tracker.db");
        SqliteHabitEntryRepository repository = new SqliteHabitEntryRepository(dbPath);

        insertEntry(dbPath, "sleep", "Sleep", "2026-06-08", 0.0, "Tired");
        insertEntry(dbPath, "exercise", "Exercise", "2026-06-08", 3.0, "Strong");
        insertEntry(dbPath, "review", "Review", "2026-06-09", 2.0, "Later");

        Map<HabitId, StoredEntry> result = repository.findEntriesByDate(LocalDate.of(2026, 6, 8));

        assertEquals(
                Map.of(
                        HabitId.of("sleep"), new StoredEntry(0.0, "Tired"),
                        HabitId.of("exercise"), new StoredEntry(3.0, "Strong")),
                result);
    }

    @Test
    void givenMatchingHabitAndDateWhenFindEntryThenReturnStoredEntry() throws Exception {
        Path dbPath = tempDir.resolve("habit_tracker.db");
        SqliteHabitEntryRepository repository = new SqliteHabitEntryRepository(dbPath);

        insertEntry(dbPath, "sleep", "Sleep", "2026-06-08", 0.0, "Tired");

        Optional<StoredEntry> result = repository.findEntry(
                LocalDate.of(2026, 6, 8),
                HabitId.of("sleep"));

        assertEquals(Optional.of(new StoredEntry(0.0, "Tired")), result);
    }

    @Test
    void givenNoMatchingHabitAndDateWhenFindEntryThenReturnEmpty() throws Exception {
        Path dbPath = tempDir.resolve("habit_tracker.db");
        SqliteHabitEntryRepository repository = new SqliteHabitEntryRepository(dbPath);

        insertEntry(dbPath, "sleep", "Sleep", "2026-06-08", 2.0);

        Optional<StoredEntry> result = repository.findEntry(
                LocalDate.of(2026, 6, 9),
                HabitId.of("sleep"));

        assertTrue(result.isEmpty());
    }

    @Test
    void givenMissingEntryWhenCreateEntryThenPersistItByHabitId() throws Exception {
        Path dbPath = tempDir.resolve("habit_tracker.db");
        SqliteHabitEntryRepository repository = new SqliteHabitEntryRepository(dbPath);
        insertHabit(dbPath, "sleep", "Sleep");

        boolean created = repository.createEntry(
                LocalDate.of(2026, 6, 8),
                HabitId.of("sleep"),
                new StoredEntry(3.0, "Rested"));

        assertTrue(created);
        assertEquals(
                Optional.of(new StoredEntry(3.0, "Rested")),
                repository.findEntry(LocalDate.of(2026, 6, 8), HabitId.of("sleep")));
    }

    @Test
    void givenExistingEntryWhenCreateEntryThenDoNotOverwriteIt() throws Exception {
        Path dbPath = tempDir.resolve("habit_tracker.db");
        SqliteHabitEntryRepository repository = new SqliteHabitEntryRepository(dbPath);
        insertEntry(dbPath, "sleep", "Sleep", "2026-06-08", 2.0, "Original");

        boolean created = repository.createEntry(
                LocalDate.of(2026, 6, 8),
                HabitId.of("sleep"),
                new StoredEntry(3.0, "Replacement"));

        assertFalse(created);
        assertEquals(
                Optional.of(new StoredEntry(2.0, "Original")),
                repository.findEntry(LocalDate.of(2026, 6, 8), HabitId.of("sleep")));
    }

    @Test
    void givenUnknownHabitWhenCreateEntryThenPropagatePersistenceFailure() {
        Path dbPath = tempDir.resolve("habit_tracker.db");
        SqliteHabitEntryRepository repository = new SqliteHabitEntryRepository(dbPath);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> repository.createEntry(
                        LocalDate.of(2026, 6, 8),
                        HabitId.of("unknown"),
                        new StoredEntry(3.0, "")));

        assertEquals("Failed to create entry", exception.getMessage());
    }

    @Test
    void givenExistingEntryWhenUpdateEntryThenReplaceStoredValues() throws Exception {
        Path dbPath = tempDir.resolve("habit_tracker.db");
        SqliteHabitEntryRepository repository = new SqliteHabitEntryRepository(dbPath);
        insertEntry(dbPath, "sleep", "Sleep", "2026-06-08", 2.0, "Original");

        boolean updated = repository.updateEntry(
                LocalDate.of(2026, 6, 8),
                HabitId.of("sleep"),
                new StoredEntry(3.0, "Corrected"));

        assertTrue(updated);
        assertEquals(
                Optional.of(new StoredEntry(3.0, "Corrected")),
                repository.findEntry(LocalDate.of(2026, 6, 8), HabitId.of("sleep")));
    }

    @Test
    void givenMissingEntryWhenUpdateEntryThenDoNotCreateIt() throws Exception {
        Path dbPath = tempDir.resolve("habit_tracker.db");
        SqliteHabitEntryRepository repository = new SqliteHabitEntryRepository(dbPath);
        insertHabit(dbPath, "sleep", "Sleep");

        boolean updated = repository.updateEntry(
                LocalDate.of(2026, 6, 8),
                HabitId.of("sleep"),
                new StoredEntry(3.0, "Corrected"));

        assertFalse(updated);
        assertTrue(repository.findEntry(
                LocalDate.of(2026, 6, 8),
                HabitId.of("sleep")).isEmpty());
    }

    private static void insertEntry(
            Path dbPath,
            String habitId,
            String habitName,
            String date,
            double score) throws Exception {
        insertEntry(dbPath, habitId, habitName, date, score, "");
    }

    private static void insertEntry(
            Path dbPath,
            String habitId,
            String habitName,
            String date,
            double score,
            String note) throws Exception {

        insertHabit(dbPath, habitId, habitName);

        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    INSERT INTO habit_entries (date, habit_id, score, note)
                    VALUES ('%s', '%s', %s, '%s')
                    """.formatted(date, habitId, score, note));
        }
    }

    private static void insertHabit(
            Path dbPath,
            String habitId,
            String habitName) throws Exception {

        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    INSERT INTO habits (id, name, cadence, active)
                    VALUES ('%s', '%s', 'DAILY', 1)
                    """.formatted(habitId, habitName));
        }
    }
}
