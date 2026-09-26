package com.raposo.habittracker.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;
import com.raposo.habittracker.infrastructure.sqlite.SqliteHabitEntryRepository;
import com.raposo.habittracker.infrastructure.sqlite.SqliteHabitRepository;

@ResourceLock("SYSTEM_OUT")
class CommandParserTest {
    @TempDir
    Path tempDir;

    private SqliteHabitEntryRepository entries;
    private final HabitId habitId = HabitId.of("sleep");

    @BeforeEach
    void initializeStorage() {
        Path database = tempDir.resolve("habits.db");
        entries = new SqliteHabitEntryRepository(database);
        new SqliteHabitRepository(database).create(Habit.active(habitId, "Sleep", HabitCadence.DAILY));
    }

    @Test
    void givenRemovedImportCommandWhenParseThenShowOnlySupportedCommands() {
        Command command = CommandParser.parse(new String[] { "--import" }, entries);
        assertInstanceOf(HelpCommand.class, command);
        String output = execute(command);
        assertTrue(output.contains("--query-between-dates"));
        assertTrue(output.contains("--query-last-week"));
        assertFalse(output.contains("--import"));
        assertInstanceOf(HelpCommand.class, CommandParser.parse(new String[0], entries));
    }

    @Test
    void givenStoredZeroWhenQueryDateRangeThenPrintRecordedDataWithoutImportConfiguration() {
        LocalDate date = LocalDate.of(2026, 9, 2);
        entries.createEntry(date, habitId, new StoredEntry(0.0, "Tired"));
        Command command = CommandParser.parse(
                new String[] { "--query-between-dates", date.toString(), date.toString() }, entries);
        assertInstanceOf(QueryBetweenDatesCommand.class, command);
        String output = execute(command);
        assertTrue(output.contains("Sleep"));
        assertTrue(output.contains("Tired"));
        assertTrue(output.contains(date.toString()));
        assertTrue(output.contains("0.00"));
        assertFalse(output.contains("Scoring guides:"));
        assertTrue(entries.findEntry(date, habitId).isPresent());
    }

    @Test
    void givenLastWeekEntryWhenQueryLastWeekThenPrintItsData() {
        LocalDate date = DateRange.weekOf(LocalDate.now().minusWeeks(1)).startDate();
        entries.createEntry(date, habitId, new StoredEntry(2.5, "Rested"));
        Command command = CommandParser.parse(new String[] { "--query-last-week" }, entries);
        assertInstanceOf(QueryLastWeekCommand.class, command);
        String output = execute(command);
        assertTrue(output.contains(date.toString()));
        assertTrue(output.contains("Sleep"));
        assertTrue(output.contains("Rested"));
        assertTrue(output.contains("2.50"));
    }

    @Test
    void printsMultilineGuideAfterTablesWithoutChangingScores() {
        new SqliteHabitRepository(tempDir.resolve("habits.db")).setScoringGuide(habitId, "0: tired\n3: rested");
        entries.createEntry(LocalDate.of(2026, 9, 2), habitId, new StoredEntry(1.5, null));
        String output = execute(CommandParser.parse(
                new String[] { "--query-between-dates", "2026-09-02", "2026-09-02" }, entries));
        assertTrue(output.contains("1.50"));
        assertTrue(output.contains("Scoring guides:\n\nSleep\n0: tired\n3: rested\n"));
        assertTrue(output.indexOf("Scoring guides:") > output.indexOf("Summary:"));
    }

    private String execute(Command command) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            command.execute();
        } finally {
            System.setOut(original);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
