package com.raposo.habittracker.application.habit;

public record CreateHabitInput(
        String name,
        String cadence,
        String scoringGuide) {
    public CreateHabitInput(String name, String cadence) {
        this(name, cadence, null);
    }
}
