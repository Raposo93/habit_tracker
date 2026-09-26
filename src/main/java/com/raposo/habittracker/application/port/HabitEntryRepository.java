package com.raposo.habittracker.application.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.raposo.habittracker.application.report.HabitScoringGuide;

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

    List<HabitScoringGuide> findScoringGuidesBetweenDates(LocalDate startDate, LocalDate endDate);

    Optional<LocalDate> findEarliestEntryDate();
}
