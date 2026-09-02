package com.raposo.habittracker.application.entry;

import java.time.LocalDate;
import java.util.List;

public record DailyEntryContext(
        LocalDate date,
        List<HabitEntryContext> habits) {
}
