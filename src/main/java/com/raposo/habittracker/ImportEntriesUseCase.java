package com.raposo.habittracker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class ImportEntriesUseCase {
    private static final Logger logger = Logger.getLogger(ImportEntriesUseCase.class.getName());

    private final HabitEntryReader reader;
    private final HabitEntryRepository repository;

    public ImportEntriesUseCase(HabitEntryReader reader, HabitEntryRepository repository) {
        this.reader = reader;
        this.repository = repository;
    }

    public void execute() {
        List<HabitEntry> entries = reader.readEntries();

        if (entries.isEmpty()) {
            logger.info("No entries to import");
            return;
        }

        List<HabitEntry> sortedEntries = entries.stream()
                .sorted(Comparator
                        .comparing(HabitEntry::entryDate)
                        .thenComparing(HabitEntry::habit))
                .toList();

        LocalDate startDate = sortedEntries.getFirst().entryDate();
        LocalDate endDate = sortedEntries.getLast().entryDate();

        Optional<LocalDate> latestEntryDate = repository.findLatestEntryDate();

        Map<EntryKey, StoredEntry> existingEntries = new HashMap<>(
                repository.findEntriesBetweenDates(startDate, endDate));

        List<HabitEntry> entriesToInsert = new ArrayList<>();
        List<HabitEntry> entriesToUpdate = new ArrayList<>();

        for (HabitEntry entry : sortedEntries) {

            EntryKey key = new EntryKey(entry.entryDate(), entry.habit());
            StoredEntry newEntry = new StoredEntry(entry.score(), entry.note());

            if (existingEntries.containsKey(key)) {
                StoredEntry storedEntry = existingEntries.get(key);

                if (!storedEntry.equals(newEntry)) {
                    entriesToUpdate.add(entry);
                    existingEntries.put(key, newEntry);
                    logger.info("Queued update: " + entry.entryDate() + " - " + entry.habit());
                } else {
                    logger.info("Skipped duplicate: " + entry.entryDate() + " - " + entry.habit());
                }

                continue;
            }

            if (latestEntryDate.isPresent()) {
                LocalDate latestDate = latestEntryDate.get();

                if (entry.entryDate().isBefore(latestDate)) {
                    logger.info(
                            "Skipped older entry: "
                                    + entry.entryDate()
                                    + " < "
                                    + latestDate
                                    + " ("
                                    + entry.habit()
                                    + ")");
                    continue;
                }
            }

            entriesToInsert.add(entry);
            existingEntries.put(key, newEntry);
        }

        if (!entriesToInsert.isEmpty()) {
            repository.insertEntries(entriesToInsert);
        }

        if (!entriesToUpdate.isEmpty()) {
            repository.updateEntries(entriesToUpdate);
        }

        logger.info(
                "Entries import completed: "
                        + entriesToInsert.size()
                        + " inserted, "
                        + entriesToUpdate.size()
                        + " updated");
    }
}
