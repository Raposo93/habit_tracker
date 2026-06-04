package com.raposo.habittracker.application;

import java.util.Map;

import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.StoredEntry;

public class GetHabitReportBetweenDatesUseCase {

    private final HabitEntryRepository repository;
    private final HabitReportBuilder reportBuilder;

    public GetHabitReportBetweenDatesUseCase(HabitEntryRepository repository) {
        this.repository = repository;
        this.reportBuilder = new HabitReportBuilder();
    }

    public HabitReport execute(DateRange currentRange) {
        DateRange previousRange = currentRange.previousEquivalent();
        Map<EntryKey, StoredEntry> currentEntries = repository.findEntriesBetweenDates(
                currentRange.startDate(),
                currentRange.endDate());

        Map<EntryKey, StoredEntry> previousEntries = repository.findEntriesBetweenDates(
                previousRange.startDate(),
                previousRange.endDate());

        return reportBuilder.build(
                currentEntries,
                currentRange,
                previousEntries,
                previousRange);
    }
}
