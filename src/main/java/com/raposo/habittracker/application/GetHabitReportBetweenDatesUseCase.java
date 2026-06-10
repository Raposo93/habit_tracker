package com.raposo.habittracker.application;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.StoredEntry;

public class GetHabitReportBetweenDatesUseCase {

    private final HabitEntryRepository entryRepository;
    private final HabitReportBuilder reportBuilder;

    public GetHabitReportBetweenDatesUseCase(HabitEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
        this.reportBuilder = new HabitReportBuilder();
    }

    public HabitReport execute(DateRange currentRange) {
        DateRange previousRange = currentRange.previousEquivalent();
        Map<EntryKey, StoredEntry> currentEntries = entryRepository.findEntriesBetweenDates(
                currentRange.startDate(),
                currentRange.endDate());

        Map<EntryKey, StoredEntry> previousEntries = entryRepository.findEntriesBetweenDates(
                previousRange.startDate(),
                previousRange.endDate());

        Optional<LocalDate> trackingStartDate = entryRepository.findEarliestEntryDate();

        return reportBuilder.build(
                currentEntries,
                currentRange,
                previousEntries,
                previousRange,
                trackingStartDate);
    }
}
