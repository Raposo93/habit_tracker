package com.raposo.habittracker.infrastructure.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.raposo.habittracker.application.GetDailyEntryContextUseCase;
import com.raposo.habittracker.application.GetHabitReportBetweenDatesUseCase;
import com.raposo.habittracker.application.ListHabitsUseCase;
import com.raposo.habittracker.application.RenameHabitUseCase;
import com.raposo.habittracker.application.SetHabitActiveUseCase;
import com.raposo.habittracker.application.port.HabitRepository.RenameResult;
import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;

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

    @Test
    void givenHabitWithHistoryWhenDeactivateAndReactivateThenPreserveDataAndRefreshContext() {
        Path dbPath = initializedDatabase();
        SqliteHabitRepository repository = new SqliteHabitRepository(dbPath);
        SqliteHabitEntryRepository entries = new SqliteHabitEntryRepository(dbPath);
        Habit habit = Habit.active(HabitId.of("sleep"), "Sleep", HabitCadence.WEEKLY);
        LocalDate date = LocalDate.of(2026, 9, 2);
        StoredEntry entry = new StoredEntry(0.0, "Tired");
        assertTrue(repository.create(habit));
        assertTrue(entries.createEntry(date, habit.id(), entry));
        SetHabitActiveUseCase setActive = new SetHabitActiveUseCase(repository);
        GetDailyEntryContextUseCase context = new GetDailyEntryContextUseCase(repository, entries);

        assertEquals(1, context.execute(date).habits().size());
        setActive.execute(habit.id(), false);
        setActive.execute(habit.id(), false);

        Habit inactive = Habit.inactive(habit.id(), habit.name(), habit.cadence());
        assertEquals(List.of(inactive), new ListHabitsUseCase(repository).execute());
        assertTrue(context.execute(date).habits().isEmpty());
        assertEquals(Optional.of(entry), entries.findEntry(date, habit.id()));
        assertEquals(Optional.of(inactive), new SqliteHabitRepository(dbPath).findById(habit.id()));

        setActive.execute(habit.id(), true);
        setActive.execute(habit.id(), true);

        assertEquals(Optional.of(habit), new SqliteHabitRepository(dbPath).findById(habit.id()));
        assertEquals(habit.id(), context.execute(date).habits().getFirst().habitId());
        assertEquals(Optional.of(entry), context.execute(date).habits().getFirst().entry());
    }

    @Test
    void givenMissingHabitWhenSetActiveThenDoNotCreateIt() {
        SqliteHabitRepository repository = new SqliteHabitRepository(initializedDatabase());
        assertFalse(repository.setActive(HabitId.of("missing"), true));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void givenHistoryWhenRenameThenPreserveIdentityAndShowCurrentNameInContextAndReports() {
        Path dbPath = initializedDatabase();
        SqliteHabitRepository repository = new SqliteHabitRepository(dbPath);
        SqliteHabitEntryRepository entries = new SqliteHabitEntryRepository(dbPath);
        Habit habit = Habit.active(HabitId.of("sleep"), "Sleep", HabitCadence.WEEKLY);
        LocalDate date = LocalDate.of(2026, 9, 2);
        StoredEntry entry = new StoredEntry(0.0, "Tired");
        repository.create(habit);
        entries.createEntry(date, habit.id(), entry);
        entries.createEntry(date.minusDays(1), habit.id(), new StoredEntry(2.0, "Better"));

        RenameHabitUseCase rename = new RenameHabitUseCase(repository);
        rename.execute(habit.id(), "  Rest  ");
        rename.execute(habit.id(), "Rest");

        assertEquals(Optional.of(Habit.active(habit.id(), "Rest", habit.cadence())),
                new SqliteHabitRepository(dbPath).findById(habit.id()));
        assertTrue(repository.findByExactName("Sleep").isEmpty());
        var context = new GetDailyEntryContextUseCase(repository, entries).execute(date);
        assertEquals(habit.id(), context.habits().getFirst().habitId());
        assertEquals("Rest", context.habits().getFirst().habitName());
        assertEquals(Optional.of(entry), context.habits().getFirst().entry());
        var report = new GetHabitReportBetweenDatesUseCase(entries).execute(DateRange.of(date, date));
        assertEquals("Rest", report.entries().getFirst().habit());
        assertEquals(0.0, report.entries().getFirst().score());
        assertEquals("Tired", report.entries().getFirst().note());
        assertEquals(1, report.summary().size());
        assertEquals("Rest", report.summary().getFirst().habit());
        assertEquals(2.0, report.summary().getFirst().previousPeriodScore());
    }

    @Test
    void givenInactiveDuplicateNameWhenRenameThenRejectAndPreserveBothHabits() {
        SqliteHabitRepository repository = new SqliteHabitRepository(initializedDatabase());
        Habit source = Habit.active(HabitId.of("sleep"), "Sleep", HabitCadence.DAILY);
        Habit target = Habit.inactive(HabitId.of("rest"), "Rest", HabitCadence.WEEKLY);
        repository.create(source);
        repository.create(target);

        assertEquals(RenameResult.NAME_ALREADY_EXISTS, repository.rename(source.id(), "Rest"));
        assertEquals(Optional.of(source), repository.findById(source.id()));
        assertEquals(Optional.of(target), repository.findById(target.id()));
        assertEquals(RenameResult.RENAMED, repository.rename(source.id(), "rest"));
    }

    @Test
    void givenInactiveHabitWhenRenameThenPreserveInactiveState() {
        SqliteHabitRepository repository = new SqliteHabitRepository(initializedDatabase());
        Habit habit = Habit.inactive(HabitId.of("sleep"), "Sleep", HabitCadence.WEEKLY);
        repository.create(habit);
        assertEquals(RenameResult.RENAMED, repository.rename(habit.id(), "Rest"));
        assertEquals(Optional.of(Habit.inactive(habit.id(), "Rest", habit.cadence())),
                repository.findById(habit.id()));
        assertTrue(repository.findActive().isEmpty());
    }

    @Test
    void givenMissingHabitWhenRenameThenDoNotCreateIt() {
        SqliteHabitRepository repository = new SqliteHabitRepository(initializedDatabase());
        assertEquals(RenameResult.NOT_FOUND, repository.rename(HabitId.of("missing"), "Rest"));
        assertTrue(repository.findAll().isEmpty());
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
