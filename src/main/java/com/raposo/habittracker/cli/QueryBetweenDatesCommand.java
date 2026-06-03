package com.raposo.habittracker.cli;

import java.util.Comparator;
import java.util.Map;

import com.raposo.habittracker.application.GetEntriesBetweenDatesUseCase;
import com.raposo.habittracker.domain.DateRange;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.StoredEntry;

public class QueryBetweenDatesCommand implements Command {

    private final GetEntriesBetweenDatesUseCase getEntries;
    private final DateRange range;

    public QueryBetweenDatesCommand(GetEntriesBetweenDatesUseCase getEntries, DateRange range) {
        this.getEntries = getEntries;
        this.range = range;
    }

    @Override
    public void execute() {
        Map<EntryKey, StoredEntry> entries = getEntries.execute(range);

        if (entries.isEmpty()) {
            System.out.println("No entries found between " + range.startDate() + " and " + range.endDate());
            return;
        }

        entries.entrySet().stream()
                .sorted(Comparator
                        .comparing((Map.Entry<EntryKey, StoredEntry> entry) -> entry.getKey().entryDate())
                        .thenComparing(entry -> entry.getKey().habit()))
                .forEach(entry -> System.out.println(entry.getKey() + " -> " + entry.getValue()));
    }
}
