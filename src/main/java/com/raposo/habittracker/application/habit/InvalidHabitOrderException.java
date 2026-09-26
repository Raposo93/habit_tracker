package com.raposo.habittracker.application.habit;

public class InvalidHabitOrderException extends IllegalArgumentException {
    public InvalidHabitOrderException() {
        super("Habit order must contain valid, unique habit identities");
    }
}
