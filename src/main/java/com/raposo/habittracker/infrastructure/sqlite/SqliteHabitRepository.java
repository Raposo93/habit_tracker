package com.raposo.habittracker.infrastructure.sqlite;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;

public class SqliteHabitRepository implements HabitRepository {
    private final Path dbPath;

    public SqliteHabitRepository(Path dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public boolean create(Habit habit) {
        String sql = """
                INSERT INTO habits (id, name, cadence, active, scoring_guide, display_order)
                VALUES (?, ?, ?, ?, ?, (SELECT COALESCE(MAX(display_order), -1) + 1 FROM habits))
                ON CONFLICT(name) DO NOTHING
                """;

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, habit.id().value());
            statement.setString(2, habit.name());
            statement.setString(3, habit.cadence().name());
            statement.setInt(4, habit.active() ? 1 : 0);
            statement.setString(5, habit.scoringGuide());

            return statement.executeUpdate() == 1;

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to create habit", exception);
        }
    }

    @Override
    public boolean setActive(HabitId habitId, boolean active) {
        String sql = "UPDATE habits SET active = ? WHERE id = ?";

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, active ? 1 : 0);
            statement.setString(2, habitId.value());
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to set habit active state", exception);
        }
    }

    @Override
    public RenameResult rename(HabitId habitId, String name) {
        String sql = "UPDATE habits SET name = ? WHERE id = ?";

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, habitId.value());
            return statement.executeUpdate() == 1 ? RenameResult.RENAMED : RenameResult.NOT_FOUND;
        } catch (SQLException exception) {
            if (exception instanceof SQLiteException sqliteException
                    && sqliteException.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                return RenameResult.NAME_ALREADY_EXISTS;
            }
            throw new IllegalStateException("Failed to rename habit", exception);
        }
    }

    @Override
    public boolean reorder(List<HabitId> habitIds) {
        try (Connection connection = connect(); Statement transaction = connection.createStatement()) {
            // Reserve the write transaction before reading so catalog validation and updates stay atomic.
            transaction.execute("BEGIN IMMEDIATE");
            try {
                HashSet<HabitId> storedIds = new HashSet<>();
                try (ResultSet rows = transaction.executeQuery("SELECT id FROM habits")) {
                    while (rows.next()) {
                        storedIds.add(HabitId.of(rows.getString("id")));
                    }
                }
                if (habitIds.size() != storedIds.size() || !storedIds.equals(new HashSet<>(habitIds))) {
                    transaction.execute("ROLLBACK");
                    return false;
                }
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE habits SET display_order = ? WHERE id = ?")) {
                    for (int position = 0; position < habitIds.size(); position++) {
                        update.setInt(1, position);
                        update.setString(2, habitIds.get(position).value());
                        update.addBatch();
                    }
                    update.executeBatch();
                }
                transaction.execute("COMMIT");
                return true;
            } catch (SQLException exception) {
                try {
                    transaction.execute("ROLLBACK");
                } catch (SQLException rollbackFailure) {
                    exception.addSuppressed(rollbackFailure);
                }
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to reorder habits", exception);
        }
    }

    @Override
    public boolean setScoringGuide(HabitId habitId, String scoringGuide) {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(
                "UPDATE habits SET scoring_guide = ? WHERE id = ?")) {
            statement.setString(1, scoringGuide);
            statement.setString(2, habitId.value());
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to set habit scoring guide", exception);
        }
    }

    @Override
    public Optional<Habit> findById(HabitId id) {
        String sql = """
                SELECT id, name, cadence, active, scoring_guide
                FROM habits
                WHERE id = ?
                """;

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id.value());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapHabit(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find habit by id", exception);
        }
    }

    @Override
    public List<Habit> findActive() {
        String sql = """
                SELECT id, name, cadence, active, scoring_guide
                FROM habits
                WHERE active = 1
                ORDER BY display_order, id
                """;

        return findMany(sql);
    }

    @Override
    public List<Habit> findAll() {
        String sql = """
                SELECT id, name, cadence, active, scoring_guide
                FROM habits
                ORDER BY display_order, id
                """;

        return findMany(sql);
    }

    private List<Habit> findMany(String sql) {
        List<Habit> habits = new ArrayList<>();

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                habits.add(mapHabit(resultSet));
            }

            return habits;

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find habits", exception);
        }
    }

    private Habit mapHabit(ResultSet resultSet) throws SQLException {
        return new Habit(
                HabitId.of(resultSet.getString("id")),
                resultSet.getString("name"),
                HabitCadence.valueOf(resultSet.getString("cadence").toUpperCase(Locale.ROOT)),
                resultSet.getInt("active") == 1,
                resultSet.getString("scoring_guide"));
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
}
