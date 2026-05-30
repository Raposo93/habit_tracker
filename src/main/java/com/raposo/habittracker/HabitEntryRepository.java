package com.raposo.habittracker;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HabitEntryRepository {
    Optional<LocalDate> findLatestEntryDate();

    Map<EntryKey, StoredEntry> findEntriesBetweenDates(
            LocalDate startDate,
            LocalDate endDate);

    void insertEntries(List<HabitEntry> entries);

    void updateEntries(List<HabitEntry> entries);
}
