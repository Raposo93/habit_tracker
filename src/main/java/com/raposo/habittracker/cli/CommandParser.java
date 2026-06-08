package com.raposo.habittracker.cli;

import com.raposo.habittracker.application.GetHabitReportBetweenDatesUseCase;
import com.raposo.habittracker.application.ImportEntriesUseCase;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
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
            HabitEntryRepository entryRepository,
            HabitRepository habitRepository) {
        if (args.length == 0) {
            return new HelpCommand();
        }

        return switch (args[0]) {
            case "--import" -> {
                GoogleAuth auth = new GoogleAuth(config);

                SheetReader reader = new SheetReader(
                        auth.createSheetsService(),
                        config.spreadsheetId());
                ImportEntriesUseCase importEntries = new ImportEntriesUseCase(
                        reader,
                        entryRepository,
                        habitRepository);

                yield new ImportCommand(importEntries);
            }

            case "--query-between-dates" -> {
                if (args.length != 3) {
                    yield new HelpCommand();
                }

                DateRange range = DateRange.between(args[1], args[2]);

                GetHabitReportBetweenDatesUseCase getReport = new GetHabitReportBetweenDatesUseCase(entryRepository);

                HabitReportFormatter formatter = new MarkdownHabitReportFormatter();

                yield new QueryBetweenDatesCommand(getReport, formatter, range);
            }

            case "--query-last-week" -> {
                GetHabitReportBetweenDatesUseCase getReport = new GetHabitReportBetweenDatesUseCase(entryRepository);

                HabitReportFormatter formatter = new MarkdownHabitReportFormatter();

                yield new QueryLastWeekCommand(getReport, formatter);
            }

            default -> new HelpCommand();
        };
    }
}
