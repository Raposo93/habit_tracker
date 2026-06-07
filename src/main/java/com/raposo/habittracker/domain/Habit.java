package com.raposo.habittracker.domain;

public record Habit(HabitId id,
        String name,
        HabitCadence cadence,
        boolean active) {

    public Habit {
        if (id == null) {
            throw new IllegalArgumentException("HabitId cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Habit name cannot be null or blank");
        }
        if (cadence == null) {
            throw new IllegalArgumentException("Habit cadence cannot be null");
        }

        name = name.trim();
    }

    public static Habit active(HabitId id, String name, HabitCadence cadence) {
        return new Habit(id, name, cadence, true);
    }

    public static Habit inactive(HabitId id, String name, HabitCadence cadence) {
        return new Habit(id, name, cadence, false);
    }
}
