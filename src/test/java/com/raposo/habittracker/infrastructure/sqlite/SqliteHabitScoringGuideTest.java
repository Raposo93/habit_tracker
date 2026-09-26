package com.raposo.habittracker.infrastructure.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;

class SqliteHabitScoringGuideTest {
    @TempDir Path tempDir;

    @Test
    void upgradesExistingOrderedDatabaseAndPreservesGuidesAcrossRestarts() throws Exception {
        Path database = tempDir.resolve("habits.db");
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database);
                var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE habits (id TEXT PRIMARY KEY, name TEXT NOT NULL UNIQUE, cadence TEXT NOT NULL, active INTEGER NOT NULL, display_order INTEGER NOT NULL)");
            statement.execute("INSERT INTO habits VALUES ('sleep', 'Sleep', 'DAILY', 0, 7)");
        }
        var entries = new SqliteHabitEntryRepository(database);
        var habits = new SqliteHabitRepository(database);
        HabitId id = HabitId.of("sleep");
        assertNull(habits.findById(id).orElseThrow().scoringGuide());
        entries.createEntry(LocalDate.of(2026, 9, 2), id, new StoredEntry(0.0, "Tired"));
        assertTrue(habits.setScoringGuide(id, "0: tired\n3: rested"));
        habits.create(new Habit(HabitId.of("other"), "Other", HabitCadence.WEEKLY, true, "Weekly guide"));
        new SqliteHabitEntryRepository(database);
        habits = new SqliteHabitRepository(database);
        assertEquals("0: tired\n3: rested", habits.findById(id).orElseThrow().scoringGuide());
        assertFalse(habits.findById(id).orElseThrow().active());
        assertEquals(List.of(id, HabitId.of("other")), habits.findAll().stream().map(Habit::id).toList());
        assertEquals(new StoredEntry(0.0, "Tired"), entries.findEntry(LocalDate.of(2026, 9, 2), id).orElseThrow());
        assertEquals(1, entries.findScoringGuidesBetweenDates(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3)).size());
        assertTrue(habits.setScoringGuide(id, null));
        assertTrue(entries.findScoringGuidesBetweenDates(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3)).isEmpty());
        assertFalse(habits.setScoringGuide(HabitId.of("missing"), "Guide"));
    }
}
