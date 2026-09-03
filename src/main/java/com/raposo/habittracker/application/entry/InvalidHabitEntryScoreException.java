package com.raposo.habittracker.application.entry;

public final class InvalidHabitEntryScoreException extends IllegalArgumentException {

    private InvalidHabitEntryScoreException(String message) {
        super(message);
    }

    public static InvalidHabitEntryScoreException missing() {
        return new InvalidHabitEntryScoreException("Score cannot be null");
    }

    public static InvalidHabitEntryScoreException outsideAllowedRange() {
        return new InvalidHabitEntryScoreException("Score must be between 0 and 3");
    }
}
