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
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;

class SqliteHabitOrderingTest {
    @TempDir
    Path tempDir;

    @Test
    void givenLegacyDatabaseWhenInitializedThenMigrateAlphabeticallyWithoutLosingHistory() throws Exception {
        Path database = tempDir.resolve("legacy.db");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE habits (
                        id TEXT PRIMARY KEY, name TEXT NOT NULL UNIQUE,
                        cadence TEXT NOT NULL CHECK (cadence IN ('DAILY', 'WEEKLY')),
                        active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1))
                    )
                    """);
            statement.execute("""
                    CREATE TABLE habit_entries (
                        date TEXT NOT NULL, habit_id TEXT NOT NULL, score REAL NOT NULL, note TEXT,
                        PRIMARY KEY (date, habit_id), FOREIGN KEY (habit_id) REFERENCES habits(id)
                    )
                    """);
            statement.execute("INSERT INTO habits VALUES ('z', 'Alpha', 'DAILY', 1), ('a', 'Zulu', 'WEEKLY', 0)");
            statement.execute("INSERT INTO habit_entries VALUES ('2026-09-02', 'z', 0, 'Tired')");
        }
        SqliteHabitEntryRepository entries = new SqliteHabitEntryRepository(database);
        SqliteHabitRepository habits = new SqliteHabitRepository(database);
        assertEquals(List.of("z", "a"), ids(habits.findAll()));
        assertEquals(List.of("z"), ids(habits.findActive()));
        assertEquals(Optional.of(new StoredEntry(0.0, "Tired")),
                entries.findEntry(LocalDate.of(2026, 9, 2), HabitId.of("z")));
        assertEquals(Habit.inactive(HabitId.of("a"), "Zulu", HabitCadence.WEEKLY),
                habits.findById(HabitId.of("a")).orElseThrow());

        assertTrue(habits.reorder(List.of(HabitId.of("a"), HabitId.of("z"))));
        new SqliteHabitEntryRepository(database);
        assertEquals(List.of("a", "z"), ids(new SqliteHabitRepository(database).findAll()));
    }

    @Test
    void givenConfiguredOrderWhenRenameReactivateAndCreateThenKeepPositionsAndAppendNewHabit() {
        Path database = tempDir.resolve("new.db");
        new SqliteHabitEntryRepository(database);
        SqliteHabitRepository habits = new SqliteHabitRepository(database);
        habits.create(habit("z", "Zulu", true));
        habits.create(habit("a", "Alpha", false));
        assertEquals(List.of("z", "a"), ids(habits.findAll()));
        assertTrue(habits.reorder(List.of(HabitId.of("a"), HabitId.of("z"))));
        assertTrue(habits.reorder(List.of(HabitId.of("a"), HabitId.of("z"))));
        habits.rename(HabitId.of("a"), "Zzz");
        habits.setActive(HabitId.of("a"), true);
        habits.create(habit("b", "Beta", true));
        assertEquals(List.of("a", "z", "b"), ids(habits.findAll()));
        assertEquals(List.of("a", "z", "b"), ids(habits.findActive()));
        new SqliteHabitEntryRepository(database);
        assertEquals(List.of("a", "z", "b"), ids(new SqliteHabitRepository(database).findAll()));
    }

    @Test
    void givenIncompleteUnknownOrDuplicateOrderWhenReorderThenKeepExistingPositions() {
        SqliteHabitRepository habits = initializedCatalog();
        for (List<HabitId> order : List.of(List.of(HabitId.of("a")),
                List.of(HabitId.of("a"), HabitId.of("missing")),
                List.of(HabitId.of("a"), HabitId.of("a")), List.<HabitId>of())) {
            assertFalse(habits.reorder(order));
            assertEquals(List.of("a", "b"), ids(habits.findAll()));
        }
    }

    @Test
    void givenFailureDuringPositionUpdatesWhenReorderThenRollbackAllChanges() throws Exception {
        SqliteHabitRepository habits = initializedCatalog();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + tempDir.resolve("habits.db"));
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TRIGGER reject_order BEFORE UPDATE OF display_order ON habits
                    WHEN NEW.id = 'a' BEGIN SELECT RAISE(ABORT, 'test write failure'); END
                    """);
        }
        assertThrows(IllegalStateException.class,
                () -> habits.reorder(List.of(HabitId.of("b"), HabitId.of("a"))));
        assertEquals(List.of("a", "b"), ids(habits.findAll()));
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + tempDir.resolve("habits.db"));
                Statement statement = connection.createStatement();
                var rows = statement.executeQuery("SELECT display_order FROM habits WHERE id = 'b'")) {
            assertTrue(rows.next());
            assertEquals(1, rows.getInt(1));
        }
    }

    @Test
    void givenEmptyCatalogWhenReorderThenSucceed() {
        Path database = tempDir.resolve("empty.db");
        new SqliteHabitEntryRepository(database);
        assertTrue(new SqliteHabitRepository(database).reorder(List.of()));
    }

    private SqliteHabitRepository initializedCatalog() {
        Path database = tempDir.resolve("habits.db");
        new SqliteHabitEntryRepository(database);
        SqliteHabitRepository habits = new SqliteHabitRepository(database);
        habits.create(habit("a", "Alpha", true));
        habits.create(habit("b", "Beta", true));
        return habits;
    }

    private Habit habit(String id, String name, boolean active) {
        return new Habit(HabitId.of(id), name, HabitCadence.DAILY, active);
    }

    private List<String> ids(List<Habit> habits) {
        return habits.stream().map(habit -> habit.id().value()).toList();
    }
}
