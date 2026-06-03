package com.raposo.habittracker.application;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.StoredEntry;

public class GetWeekEntriesUseCase {

    private final HabitEntryRepository repository;

    public GetWeekEntriesUseCase(HabitEntryRepository repository) {
        this.repository = repository;
    }

    public Map<EntryKey, StoredEntry> execute(LocalDate referenceDate) {
        LocalDate startDate = referenceDate.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );
        LocalDate endDate = startDate.plusDays(6);

        return repository.findEntriesBetweenDates(startDate, endDate);
    }
}
