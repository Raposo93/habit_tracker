package com.raposo.habittracker.infrastructure.sqlite;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
                INSERT INTO habits (id, name, cadence, active)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(name) DO NOTHING
                """;

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, habit.id().value());
            statement.setString(2, habit.name());
            statement.setString(3, habit.cadence().name());
            statement.setInt(4, habit.active() ? 1 : 0);

            return statement.executeUpdate() == 1;

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to create habit", exception);
        }
    }

    @Override
    public Optional<Habit> findById(HabitId id) {
        String sql = """
                SELECT id, name, cadence, active
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
    public Optional<Habit> findByExactName(String name) {
        String sql = """
                SELECT id, name, cadence, active
                FROM habits
                WHERE name = ?
                """;

        try (
                Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapHabit(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find habit by exact name", exception);
        }
    }

    @Override
    public List<Habit> findActive() {
        String sql = """
                SELECT id, name, cadence, active
                FROM habits
                WHERE active = 1
                ORDER BY name
                """;

        return findMany(sql);
    }

    @Override
    public List<Habit> findAll() {
        String sql = """
                SELECT id, name, cadence, active
                FROM habits
                ORDER BY name
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
                resultSet.getInt("active") == 1);
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
}
