package com.raposo.habittracker.application;

import java.time.LocalDate;
import java.util.Map;

import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.StoredEntry;

public class GetWeekEntriesUseCase {

    private final HabitEntryRepository repository;

    public GetWeekEntriesUseCase(HabitEntryRepository repository) {
        this.repository = repository;
    }

    public Map<EntryKey, StoredEntry> execute(LocalDate referenceDate) {
        DateRange range = DateRange.weekOf(referenceDate);

        return repository.findEntriesBetweenDates(
                range.startDate(),
                range.endDate());
    }
}
