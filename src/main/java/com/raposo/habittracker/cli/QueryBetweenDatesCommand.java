package com.raposo.habittracker.cli;

import com.raposo.habittracker.application.GetHabitReportBetweenDatesUseCase;
import com.raposo.habittracker.application.report.HabitReport;
import com.raposo.habittracker.cli.formatter.HabitReportFormatter;
import com.raposo.habittracker.domain.DateRange;

public class QueryBetweenDatesCommand implements Command {

    private final GetHabitReportBetweenDatesUseCase getReport;
    private final HabitReportFormatter formatter;
    private final DateRange range;

    public QueryBetweenDatesCommand(
            GetHabitReportBetweenDatesUseCase getReport,
            HabitReportFormatter formatter,
            DateRange range
    ) {
        this.getReport = getReport;
        this.formatter = formatter;
        this.range = range;
    }

    @Override
    public void execute() {
        HabitReport report = getReport.execute(range);
        System.out.println(formatter.format(report));
    }
}
