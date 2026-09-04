package com.raposo.habittracker.application.habit;

public class InvalidHabitCadenceException extends IllegalArgumentException {

    private InvalidHabitCadenceException(String message) {
        super(message);
    }

    public static InvalidHabitCadenceException missing() {
        return new InvalidHabitCadenceException("Habit cadence cannot be null or blank");
    }

    public static InvalidHabitCadenceException unsupported(String cadence) {
        return new InvalidHabitCadenceException("Unsupported habit cadence: " + cadence);
    }
}
