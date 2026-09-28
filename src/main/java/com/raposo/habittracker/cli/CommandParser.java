package com.raposo.habittracker.cli;

import java.time.DateTimeException;

import com.raposo.habittracker.application.GetHabitReportBetweenDatesUseCase;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.cli.formatter.HabitReportFormatter;
import com.raposo.habittracker.cli.formatter.MarkdownHabitReportFormatter;
import com.raposo.habittracker.domain.DateRange;

public class CommandParser {
    public static Command parse(
            String[] args,
            HabitEntryRepository entryRepository) {
        if (args.length == 0) {
            return new HelpCommand();
        }

        return switch (args[0]) {
            case "--query-between-dates" -> {
                if (args.length != 3) {
                    yield new HelpCommand();
                }

                DateRange range;
                try {
                    range = DateRange.between(args[1], args[2]);
                } catch (DateTimeException | IllegalArgumentException exception) {
                    yield new HelpCommand();
                }

                GetHabitReportBetweenDatesUseCase getReport = new GetHabitReportBetweenDatesUseCase(entryRepository);

                HabitReportFormatter formatter = new MarkdownHabitReportFormatter();

                yield new QueryBetweenDatesCommand(getReport, formatter, range);
            }

            case "--query-last-week" -> {
                if (args.length != 1) {
                    yield new HelpCommand();
                }

                GetHabitReportBetweenDatesUseCase getReport = new GetHabitReportBetweenDatesUseCase(entryRepository);

                HabitReportFormatter formatter = new MarkdownHabitReportFormatter();

                yield new QueryLastWeekCommand(getReport, formatter);
            }

            default -> new HelpCommand();
        };
    }
}
