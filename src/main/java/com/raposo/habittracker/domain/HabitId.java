package com.raposo.habittracker.domain;

public record HabitId(String value) {

    public HabitId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("HabitId cannot be null or blank");
        }

        value = value.trim();
    }

    public static HabitId of(String value) {
        return new HabitId(value);
    }
}
