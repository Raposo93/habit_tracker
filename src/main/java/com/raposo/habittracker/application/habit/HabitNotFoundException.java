package com.raposo.habittracker.application.habit;

import com.raposo.habittracker.domain.HabitId;

public class HabitNotFoundException extends RuntimeException {
    public HabitNotFoundException(HabitId habitId) {
        super("Habit not found: " + habitId.value());
    }
}
