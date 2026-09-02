package com.raposo.habittracker.application.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.HabitEntry;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;

public interface HabitEntryRepository {
    Optional<LocalDate> findLatestEntryDate();

    Map<EntryKey, StoredEntry> findEntriesBetweenDates(
            LocalDate startDate,
            LocalDate endDate);

    Map<HabitId, StoredEntry> findEntriesByDate(LocalDate date);

    void insertEntries(List<HabitEntry> entries);

    void updateEntries(List<HabitEntry> entries);

    Optional<LocalDate> findEarliestEntryDate();
}
