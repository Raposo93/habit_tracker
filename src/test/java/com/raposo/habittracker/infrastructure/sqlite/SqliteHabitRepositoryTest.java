package com.raposo.habittracker.infrastructure.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;

class SqliteHabitRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void givenNewActiveHabitWhenCreateThenPersistAndMakeItImmediatelyAvailable() {
        Path dbPath = initializedDatabase();
        SqliteHabitRepository repository = new SqliteHabitRepository(dbPath);
        Habit habit = Habit.active(
                HabitId.of("meditation-id"),
                "Meditation",
                HabitCadence.DAILY);

        boolean created = repository.create(habit);

        assertTrue(created);
        assertEquals(Optional.of(habit), repository.findById(habit.id()));
        assertTrue(repository.findActive().contains(habit));
    }

    @Test
    void givenInactiveHabitWithSameNameWhenCreateThenDoNotReplaceIt() {
        Path dbPath = initializedDatabase();
        SqliteHabitRepository repository = new SqliteHabitRepository(dbPath);
        Habit existing = Habit.inactive(
                HabitId.of("existing-id"),
                "Meditation",
                HabitCadence.WEEKLY);
        Habit duplicate = Habit.active(
                HabitId.of("duplicate-id"),
                "Meditation",
                HabitCadence.DAILY);
        assertTrue(repository.create(existing));

        boolean created = repository.create(duplicate);

        assertFalse(created);
        assertEquals(Optional.of(existing), repository.findByExactName("Meditation"));
        assertTrue(repository.findById(duplicate.id()).isEmpty());
    }

    @Test
    void givenNamesThatDifferOnlyByCaseWhenCreateThenPersistBoth() {
        Path dbPath = initializedDatabase();
        SqliteHabitRepository repository = new SqliteHabitRepository(dbPath);

        assertTrue(repository.create(Habit.active(
                HabitId.of("upper-id"),
                "Sleep",
                HabitCadence.DAILY)));
        assertTrue(repository.create(Habit.active(
                HabitId.of("lower-id"),
                "sleep",
                HabitCadence.DAILY)));
    }

    @Test
    void givenActiveAndInactiveHabitsWhenFindAllThenReturnBoth() throws Exception {
        Path dbPath = initializedDatabase();
        insertHabit(dbPath, "exercise", "Exercise", "DAILY", true);
        insertHabit(dbPath, "review", "Review", "WEEKLY", false);

        List<Habit> result = new SqliteHabitRepository(dbPath).findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(
                Habit.active(HabitId.of("exercise"), "Exercise", HabitCadence.DAILY)));
        assertTrue(result.contains(
                Habit.inactive(HabitId.of("review"), "Review", HabitCadence.WEEKLY)));
    }

    private Path initializedDatabase() {
        Path dbPath = tempDir.resolve("habit_tracker.db");
        new SqliteHabitEntryRepository(dbPath);
        return dbPath;
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
