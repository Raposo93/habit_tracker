package com.raposo.habittracker;

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
                SELECT date, habit, score, note
                FROM habit_entries
                WHERE date BETWEEN ? AND ?
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
    public void insertEntries(List<HabitEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO habit_entries (date, habit, score, note)
                VALUES (?, ?, ?, ?)
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
                WHERE date = ? AND habit = ?
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

        String sql = """
                CREATE TABLE IF NOT EXISTS habit_entries (
                    date TEXT NOT NULL,
                    habit TEXT NOT NULL,
                    score REAL NOT NULL,
                    note TEXT,
                    PRIMARY KEY (date, habit)
                )
                """;

        try (
                Connection connection = connect();
                Statement statement = connection.createStatement()) {

            statement.execute(sql);

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to create habit_entries table", exception);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
}