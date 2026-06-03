package com.raposo.habittracker.cli;

import com.raposo.habittracker.application.GetEntriesBetweenDatesUseCase;
import com.raposo.habittracker.application.GetHabitReportBetweenDatesUseCase;
import com.raposo.habittracker.application.GetWeekEntriesUseCase;
import com.raposo.habittracker.application.ImportEntriesUseCase;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.cli.formatter.HabitReportFormatter;
import com.raposo.habittracker.cli.formatter.MarkdownHabitReportFormatter;
import com.raposo.habittracker.config.Config;
import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.infrastructure.google.GoogleAuth;
import com.raposo.habittracker.infrastructure.google.SheetReader;

public class CommandParser {
    public static Command parse(
            String[] args,
            Config config,
            HabitEntryRepository repository) {
        if (args.length == 0) {
            return new HelpCommand();
        }

        return switch (args[0]) {
            case "--import" -> {
                GoogleAuth auth = new GoogleAuth(config);

                SheetReader reader = new SheetReader(
                        auth.createSheetsService(),
                        config.spreadsheetId());
                ImportEntriesUseCase importEntries = new ImportEntriesUseCase(reader, repository);

                yield new ImportCommand(importEntries);
            }

            case "--query-btw-dates" -> {
                if (args.length != 3) {
                    yield new HelpCommand();
                }

                DateRange range = DateRange.between(args[1], args[2]);

                GetHabitReportBetweenDatesUseCase getReport = new GetHabitReportBetweenDatesUseCase(repository);

                HabitReportFormatter formatter = new MarkdownHabitReportFormatter();

                yield new QueryBetweenDatesCommand(getReport, formatter, range);
            }

            case "--query-last-week" -> {
                GetWeekEntriesUseCase getWeekEntries = new GetWeekEntriesUseCase(repository);

                yield new QueryLastWeekCommand(getWeekEntries);
            }

            default -> new HelpCommand();
        };
    }
}
