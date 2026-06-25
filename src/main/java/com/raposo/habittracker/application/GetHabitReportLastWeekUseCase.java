package com.raposo.habittracker.application;

import java.time.LocalDate;

import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.domain.DateRange;

public class GetHabitReportLastWeekUseCase {

    private final GetHabitReportBetweenDatesUseCase getReportBetweenDates;

    public GetHabitReportLastWeekUseCase(
            GetHabitReportBetweenDatesUseCase getReportBetweenDates
    ) {
        this.getReportBetweenDates = getReportBetweenDates;
    }

    public HabitReport execute() {
        LocalDate referenceDate = LocalDate.now().minusWeeks(1);
        DateRange range = DateRange.weekOf(referenceDate);

        return getReportBetweenDates.execute(range);
    }
}
