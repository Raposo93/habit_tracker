package com.raposo.habittracker.web.entry;

import com.raposo.habittracker.application.entry.DailyEntryContext;
import com.raposo.habittracker.application.entry.HabitEntryContext;
import com.raposo.habittracker.domain.StoredEntry;
import com.raposo.habittracker.web.entry.DailyEntryResponse.EntryResponse;
import com.raposo.habittracker.web.entry.DailyEntryResponse.HabitResponse;

public class DailyEntryResponseMapper {

    public DailyEntryResponse toResponse(DailyEntryContext context) {
        return new DailyEntryResponse(
                context.date(),
                context.habits().stream()
                        .map(this::toHabitResponse)
                        .toList());
    }

    private HabitResponse toHabitResponse(HabitEntryContext habit) {
        EntryResponse entry = habit.entry()
                .map(this::toEntryResponse)
                .orElse(null);

        return new HabitResponse(
                habit.habitId().value(),
                habit.habitName(),
                entry);
    }

    private EntryResponse toEntryResponse(StoredEntry entry) {
        return new EntryResponse(entry.score(), entry.note());
    }
}
