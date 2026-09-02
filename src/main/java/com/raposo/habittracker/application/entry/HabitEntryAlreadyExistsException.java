package com.raposo.habittracker.application.entry;

import java.time.LocalDate;

import com.raposo.habittracker.domain.HabitId;

public class HabitEntryAlreadyExistsException extends RuntimeException {

    public HabitEntryAlreadyExistsException(LocalDate date, HabitId habitId) {
        super("Entry already exists for habit " + habitId.value() + " on " + date);
    }
}
