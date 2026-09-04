package com.raposo.habittracker.application.habit;

public class InvalidHabitNameException extends IllegalArgumentException {

    public InvalidHabitNameException() {
        super("Habit name cannot be null or blank");
    }
}
