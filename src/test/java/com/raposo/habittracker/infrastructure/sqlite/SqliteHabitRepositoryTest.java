package com.raposo.habittracker.infrastructure.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;

class SqliteHabitRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void givenActiveAndInactiveHabitsWhenFindAllThenReturnBoth() throws Exception {
        Path dbPath = tempDir.resolve("habit_tracker.db");
        new SqliteHabitEntryRepository(dbPath);
        insertHabit(dbPath, "exercise", "Exercise", "DAILY", true);
        insertHabit(dbPath, "review", "Review", "WEEKLY", false);

        List<Habit> result = new SqliteHabitRepository(dbPath).findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(
                Habit.active(HabitId.of("exercise"), "Exercise", HabitCadence.DAILY)));
        assertTrue(result.contains(
                Habit.inactive(HabitId.of("review"), "Review", HabitCadence.WEEKLY)));
    }

    private static void insertHabit(
            Path dbPath,
            String id,
            String name,
            String cadence,
            boolean active) throws Exception {
        String sql = """
                INSERT INTO habits (id, name, cadence, active)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, name);
            statement.setString(3, cadence);
            statement.setInt(4, active ? 1 : 0);
            statement.executeUpdate();
        }
    }
}
