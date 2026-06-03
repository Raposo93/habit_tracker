package com.raposo.habittracker.cli;

import java.time.LocalDate;

import com.raposo.habittracker.application.GetHabitReportBetweenDatesUseCase;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.cli.formatter.HabitReportFormatter;
import com.raposo.habittracker.domain.DateRange;

public class QueryLastWeekCommand implements Command {

    private final GetHabitReportBetweenDatesUseCase getReport;
    private final HabitReportFormatter formatter;

    public QueryLastWeekCommand(
            GetHabitReportBetweenDatesUseCase getReport,
            HabitReportFormatter formatter
    ) {
        this.getReport = getReport;
        this.formatter = formatter;
    }

    @Override
    public void execute() {
        LocalDate referenceDate = LocalDate.now().minusWeeks(1);
        DateRange range = DateRange.weekOf(referenceDate);

        HabitReport report = getReport.execute(range);

        System.out.println(formatter.format(report));
    }
}
