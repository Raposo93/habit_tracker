package com.raposo.habittracker;

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

                SheetReader reader = new SheetReader(auth, config.SPREADSHEET_ID);

                ImportEntriesUseCase importEntries = new ImportEntriesUseCase(reader, repository);

                yield new ImportCommand(importEntries);
            }

            case "--query-btw-dates" -> {
                if (args.length != 3) {
                    yield new HelpCommand();
                }

                DateRange range = DateRange.between(args[1], args[2]);

                GetEntriesBetweenDatesUseCase getEntries = new GetEntriesBetweenDatesUseCase(repository);

                yield new QueryBetweenDatesCommand(getEntries, range);
            }

            case "--query-last-week" -> {
                GetWeekEntriesUseCase getWeekEntries = new GetWeekEntriesUseCase(repository);

                yield new QueryLastWeekCommand(getWeekEntries);
            }

            default -> new HelpCommand();
        };
    }
}
