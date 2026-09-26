package com.raposo.habittracker.application.port;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;

public interface HabitEntryRepository {
    Map<EntryKey, StoredEntry> findEntriesBetweenDates(
            LocalDate startDate,
            LocalDate endDate);

    Map<HabitId, StoredEntry> findEntriesByDate(LocalDate date);

    Optional<StoredEntry> findEntry(LocalDate date, HabitId habitId);

    boolean createEntry(LocalDate date, HabitId habitId, StoredEntry entry);

    boolean updateEntry(LocalDate date, HabitId habitId, StoredEntry entry);

    Optional<LocalDate> findEarliestEntryDate();
}
