package com.raposo.habittracker.application.habit;

public class HabitCatalogChangedException extends RuntimeException {
    public HabitCatalogChangedException() {
        super("Habit order must match the current complete catalog");
    }
}
