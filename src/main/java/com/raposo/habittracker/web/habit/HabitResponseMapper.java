package com.raposo.habittracker.web.habit;

import java.util.List;

import com.raposo.habittracker.domain.Habit;

public class HabitResponseMapper {

    public HabitCatalogResponse toCatalogResponse(List<Habit> habits) {
        return new HabitCatalogResponse(
                habits.stream()
                        .map(this::toResponse)
                        .toList());
    }

    public HabitResponse toResponse(Habit habit) {
        return new HabitResponse(
                habit.id().value(),
                habit.name(),
                habit.cadence().name(),
                habit.active());
    }
}
