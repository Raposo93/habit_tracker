package com.raposo.habittracker.web.entry;

import java.time.LocalDate;
import java.util.List;

public record DailyEntryResponse(
        LocalDate date,
        List<HabitResponse> habits) {

    public record HabitResponse(
            String habitId,
            String habitName,
            EntryResponse entry) {
    }

    public record EntryResponse(
            double score,
            String note) {
    }
}
