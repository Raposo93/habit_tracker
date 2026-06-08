package com.raposo.habittracker.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import com.raposo.habittracker.application.port.HabitEntryReader;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.EntryKey;
import com.raposo.habittracker.domain.HabitEntry;
import com.raposo.habittracker.domain.StoredEntry;

public class ImportEntriesUseCase {
    private static final Logger logger = Logger.getLogger(ImportEntriesUseCase.class.getName());

    private final HabitEntryReader reader;
    private final HabitEntryRepository entryRepository;
    private final HabitRepository habitRepository;

    public ImportEntriesUseCase(
            HabitEntryReader reader,
            HabitEntryRepository entryRepository,
            HabitRepository habitRepository) {
        this.reader = Objects.requireNonNull(reader);
        this.entryRepository = Objects.requireNonNull(entryRepository);
        this.habitRepository = Objects.requireNonNull(habitRepository);
    }

    public void execute() {
        List<HabitEntry> entries = reader.readEntries();

        if (entries.isEmpty()) {
            logger.info("No entries to import");
            return;
        }

        validateKnownHabits(entries);

        List<HabitEntry> sortedEntries = entries.stream()
                .sorted(Comparator
                        .comparing(HabitEntry::entryDate)
                        .thenComparing(HabitEntry::habit))
                .toList();

        LocalDate startDate = sortedEntries.getFirst().entryDate();
        LocalDate endDate = sortedEntries.getLast().entryDate();

        Optional<LocalDate> latestEntryDate = entryRepository.findLatestEntryDate();

        Map<EntryKey, StoredEntry> existingEntries = new HashMap<>(
                entryRepository.findEntriesBetweenDates(startDate, endDate));

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

            if (shouldSkipNewEntryOlderThanLatestStoredDate(entry, latestEntryDate)) {
                LocalDate latestDate = latestEntryDate.get();

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

            entriesToInsert.add(entry);
            existingEntries.put(key, newEntry);
        }

        if (!entriesToInsert.isEmpty()) {
            entryRepository.insertEntries(entriesToInsert);
        }

        if (!entriesToUpdate.isEmpty()) {
            entryRepository.updateEntries(entriesToUpdate);
        }

        logger.info(
                "Entries import completed: "
                        + entriesToInsert.size()
                        + " inserted, "
                        + entriesToUpdate.size()
                        + " updated");
    }

    private boolean shouldSkipNewEntryOlderThanLatestStoredDate(
            HabitEntry entry,
            Optional<LocalDate> latestEntryDate) {
        return latestEntryDate.isPresent()
                && entry.entryDate().isBefore(latestEntryDate.get());
    }

    private void validateKnownHabits(List<HabitEntry> entries) {
        entries.stream()
                .map(HabitEntry::habit)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .filter(habit -> habitRepository.findByExactName(habit).isEmpty())
                .findFirst()
                .ifPresent(habit -> {
                    throw new IllegalArgumentException(
                            "Unknown habit from Sheets: " + habit);
                });
    }
}
