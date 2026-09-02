package com.raposo.habittracker.infrastructure.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.HabitEntry;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;

public class SqliteHabitEntryRepository implements HabitEntryRepository {
    private final Path dbPath;

    public SqliteHabitEntryRepository(Path dbPath) {
        this.dbPath = dbPath;
        createTables();
    }

    @Override
    public Optional<LocalDate> findLatestEntryDate() {
        String sql = "SELECT MAX(date) FROM habit_entries";

        try (
                Connection connection = connect();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {

            String value = resultSet.getString(1);

            if (value == null) {
                return Optional.empty();
            }

            return Optional.of(LocalDate.parse(value));

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find latest entry date", exception);
        }
    }

    @Override
    public Map<EntryKey, StoredEntry> findEntriesBetweenDates(
            LocalDate startDate,
            LocalDate endDate) {

        String sql = """
                SELECT e.date, h.name AS habit, e.score, e.note
                FROM habit_entries e
                JOIN habits h ON h.id = e.habit_id
                WHERE e.date BETWEEN ? AND ?
                """;

        Map<EntryKey, StoredEntry> entries = new HashMap<>();

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, startDate.toString());
            statement.setString(2, endDate.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    LocalDate entryDate = LocalDate.parse(resultSet.getString("date"));
                    String habit = resultSet.getString("habit");
                    double score = resultSet.getDouble("score");
                    String note = resultSet.getString("note");

                    entries.put(
                            new EntryKey(entryDate, habit),
                            new StoredEntry(score, note));
                }
            }

            return entries;

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find entries between dates", exception);
        }
    }

    @Override
    public Map<HabitId, StoredEntry> findEntriesByDate(LocalDate date) {
        String sql = """
                SELECT habit_id, score, note
                FROM habit_entries
                WHERE date = ?
                """;

        Map<HabitId, StoredEntry> entries = new HashMap<>();

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, date.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    HabitId habitId = HabitId.of(resultSet.getString("habit_id"));
                    double score = resultSet.getDouble("score");
                    String note = resultSet.getString("note");

                    entries.put(habitId, new StoredEntry(score, note));
                }
            }

            return entries;

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find entries by date", exception);
        }
    }

    @Override
    public Optional<StoredEntry> findEntry(LocalDate date, HabitId habitId) {
        String sql = """
                SELECT score, note
                FROM habit_entries
                WHERE date = ?
                  AND habit_id = ?
                """;

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, date.toString());
            statement.setString(2, habitId.value());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new StoredEntry(
                            resultSet.getDouble("score"),
                            resultSet.getString("note")));
                }

                return Optional.empty();
            }

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find entry", exception);
        }
    }

    @Override
    public boolean createEntry(LocalDate date, HabitId habitId, StoredEntry entry) {
        String sql = """
                INSERT INTO habit_entries (date, habit_id, score, note)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(date, habit_id) DO NOTHING
                """;

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, date.toString());
            statement.setString(2, habitId.value());
            statement.setDouble(3, entry.score());
            statement.setString(4, entry.note());

            return statement.executeUpdate() == 1;

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to create entry", exception);
        }
    }

    @Override
    public boolean updateEntry(LocalDate date, HabitId habitId, StoredEntry entry) {
        String sql = """
                UPDATE habit_entries
                SET score = ?, note = ?
                WHERE date = ?
                  AND habit_id = ?
                """;

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, entry.score());
            statement.setString(2, entry.note());
            statement.setString(3, date.toString());
            statement.setString(4, habitId.value());

            return statement.executeUpdate() == 1;

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update entry", exception);
        }
    }

    @Override
    public void insertEntries(List<HabitEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO habit_entries (date, habit_id, score, note)
                VALUES (?, (SELECT id FROM habits WHERE name = ?), ?, ?)
                """;

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            for (HabitEntry entry : entries) {
                statement.setString(1, entry.entryDate().toString());
                statement.setString(2, entry.habit());
                statement.setDouble(3, entry.score());
                statement.setString(4, entry.note());
                statement.addBatch();
            }

            statement.executeBatch();

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to insert entries", exception);
        }
    }

    @Override
    public void updateEntries(List<HabitEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        String sql = """
                UPDATE habit_entries
                SET score = ?, note = ?
                WHERE date = ?
                  AND habit_id = (SELECT id FROM habits WHERE name = ?)
                """;

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            for (HabitEntry entry : entries) {
                statement.setDouble(1, entry.score());
                statement.setString(2, entry.note());
                statement.setString(3, entry.entryDate().toString());
                statement.setString(4, entry.habit());
                statement.addBatch();
            }

            statement.executeBatch();

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update entries", exception);
        }
    }

    private void createTables() {
        try {
            Path parent = dbPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create database directory", exception);
        }

        String sqlHabitEntries = """
                CREATE TABLE IF NOT EXISTS habit_entries (
                    date TEXT NOT NULL,
                    habit_id TEXT NOT NULL,
                    score REAL NOT NULL,
                    note TEXT,
                    PRIMARY KEY (date, habit_id),
                    FOREIGN KEY (habit_id) REFERENCES habits(id)
                )
                """;

        String sqlHabits = """
                CREATE TABLE IF NOT EXISTS habits (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL UNIQUE,
                    cadence TEXT NOT NULL CHECK (cadence IN ('DAILY', 'WEEKLY')),
                    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1))
                )
                """;

        try (
                Connection connection = connect();
                Statement statement = connection.createStatement()) {

            statement.execute(sqlHabits);
            statement.execute(sqlHabitEntries);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to create tables", exception);
        }
    }

    private Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    @Override
    public Optional<LocalDate> findEarliestEntryDate() {
        String sql = """
                SELECT MIN(date) FROM habit_entries
                """;

        try (
                Connection connection = connect();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {

            String value = resultSet.getString(1);

            if (value == null) {
                return Optional.empty();
            }

            return Optional.of(LocalDate.parse(value));
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find earliest entry date", exception);
        }
    }
}
