package com.raposo.habittracker.web.habit;

public record HabitResponse(
        String habitId,
        String habitName,
        String cadence,
        boolean active,
        String scoringGuide) {
    public HabitResponse(String habitId, String habitName, String cadence, boolean active) {
        this(habitId, habitName, cadence, active, null);
    }
}
