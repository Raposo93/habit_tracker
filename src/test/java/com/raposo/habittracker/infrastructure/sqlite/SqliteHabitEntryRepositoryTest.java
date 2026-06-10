package com.raposo.habittracker.infrastructure.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

    private static void insertEntry(
            Path dbPath,
            String habitId,
            String habitName,
            String date,
            double score) throws Exception {

        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    INSERT INTO habits (id, name, cadence, active)
                    VALUES ('%s', '%s', 'DAILY', 1)
                    """.formatted(habitId, habitName));

            statement.executeUpdate("""
                    INSERT INTO habit_entries (date, habit_id, score, note)
                    VALUES ('%s', '%s', %s, '')
                    """.formatted(date, habitId, score));
        }
    }
}