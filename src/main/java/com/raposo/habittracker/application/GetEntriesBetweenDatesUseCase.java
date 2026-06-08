package com.raposo.habittracker.application;

import java.util.Map;

import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.StoredEntry;

public class GetEntriesBetweenDatesUseCase {

    private final HabitEntryRepository repository;

    public GetEntriesBetweenDatesUseCase(HabitEntryRepository entryRepository) {
        this.repository = entryRepository;
    }

    public Map<EntryKey, StoredEntry> execute(DateRange range) {
        return repository.findEntriesBetweenDates(
                range.startDate(),
                range.endDate()
        );
    }
}
