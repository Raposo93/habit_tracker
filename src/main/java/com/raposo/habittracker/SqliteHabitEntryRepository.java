package com.raposo.habittracker;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SqliteHabitEntryRepository implements HabitEntryRepository {
    private final Path dbPath;

    public SqliteHabitEntryRepository(Path dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public Optional<LocalDate> findLatestEntryDate() {
        return Optional.empty();
    }

    @Override
    public Map<EntryKey, StoredEntry> findEntriesBetweenDates(
            LocalDate startDate,
            LocalDate endDate) {
        return new HashMap<>();
    }

    @Override
    public void insertEntries(List<HabitEntry> entries) {
    }

    @Override
    public void updateEntries(List<HabitEntry> entries) {
    }
}
