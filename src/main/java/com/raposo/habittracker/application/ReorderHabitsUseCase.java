package com.raposo.habittracker.application;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import com.raposo.habittracker.application.habit.HabitCatalogChangedException;
import com.raposo.habittracker.application.habit.InvalidHabitOrderException;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.HabitId;

public class ReorderHabitsUseCase {
    private final HabitRepository habitRepository;

    public ReorderHabitsUseCase(HabitRepository habitRepository) {
        this.habitRepository = Objects.requireNonNull(habitRepository);
    }

    public void execute(List<String> habitIds) {
        if (habitIds == null || habitIds.stream().anyMatch(id -> id == null || id.isBlank())) {
            throw new InvalidHabitOrderException();
        }
        List<HabitId> ids = habitIds.stream().map(HabitId::of).toList();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new InvalidHabitOrderException();
        }
        if (!habitRepository.reorder(ids)) {
            throw new HabitCatalogChangedException();
        }
    }
}
