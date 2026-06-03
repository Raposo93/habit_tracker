package com.raposo.habittracker.application;

import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.domain.DateRange;

public class GetHabitReportBetweenDatesUseCase {

    private final HabitEntryRepository repository;
    private final HabitReportBuilder reportBuilder;

    public GetHabitReportBetweenDatesUseCase(HabitEntryRepository repository) {
        this.repository = repository;
        this.reportBuilder = new HabitReportBuilder();
    }

    public HabitReport execute(DateRange range) {
        return reportBuilder.build(
                repository.findEntriesBetweenDates(
                        range.startDate(),
                        range.endDate()
                )
        );
    }
}