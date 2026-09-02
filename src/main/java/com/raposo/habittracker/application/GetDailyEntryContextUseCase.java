package com.raposo.habittracker.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.raposo.habittracker.application.entry.DailyEntryContext;
import com.raposo.habittracker.application.entry.HabitEntryContext;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;

public class GetDailyEntryContextUseCase {

    private final HabitRepository habitRepository;
    private final HabitEntryRepository entryRepository;

    public GetDailyEntryContextUseCase(
            HabitRepository habitRepository,
            HabitEntryRepository entryRepository) {
        this.habitRepository = habitRepository;
        this.entryRepository = entryRepository;
    }

    public DailyEntryContext execute(LocalDate date) {
        Map<HabitId, StoredEntry> entriesByHabitId = entryRepository.findEntriesByDate(date);

        List<HabitEntryContext> habits = habitRepository.findActive().stream()
                .map(habit -> new HabitEntryContext(
                        habit.id(),
                        habit.name(),
                        Optional.ofNullable(entriesByHabitId.get(habit.id()))))
                .toList();

        return new DailyEntryContext(date, habits);
    }
}
