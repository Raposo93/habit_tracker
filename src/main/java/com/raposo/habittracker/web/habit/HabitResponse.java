package com.raposo.habittracker.web.habit;

public record HabitResponse(
        String habitId,
        String habitName,
        String cadence,
        boolean active) {
}
