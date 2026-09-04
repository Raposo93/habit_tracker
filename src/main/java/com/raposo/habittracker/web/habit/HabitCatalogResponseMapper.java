package com.raposo.habittracker.web.habit;

import java.util.List;

import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.web.habit.HabitCatalogResponse.HabitResponse;

public class HabitCatalogResponseMapper {

    public HabitCatalogResponse toResponse(List<Habit> habits) {
        return new HabitCatalogResponse(
                habits.stream()
                        .map(this::toHabitResponse)
                        .toList());
    }

    private HabitResponse toHabitResponse(Habit habit) {
        return new HabitResponse(
                habit.id().value(),
                habit.name(),
                habit.cadence().name(),
                habit.active());
    }
}
