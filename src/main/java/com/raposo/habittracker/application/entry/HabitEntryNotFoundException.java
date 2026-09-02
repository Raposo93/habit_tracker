package com.raposo.habittracker.application.entry;

import java.time.LocalDate;

import com.raposo.habittracker.domain.HabitId;

public class HabitEntryNotFoundException extends RuntimeException {

    public HabitEntryNotFoundException(LocalDate date, HabitId habitId) {
        super("Entry not found for habit " + habitId.value() + " on " + date);
    }
}
