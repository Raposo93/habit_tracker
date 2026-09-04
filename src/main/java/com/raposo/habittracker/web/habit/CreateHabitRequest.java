package com.raposo.habittracker.web.habit;

public record CreateHabitRequest(
        String habitName,
        String cadence) {
}
