package com.raposo.habittracker.application.entry;

import com.raposo.habittracker.domain.HabitId;

public class UnknownHabitException extends RuntimeException {

    public UnknownHabitException(HabitId habitId) {
        super("Unknown habit: " + habitId.value());
    }
}
