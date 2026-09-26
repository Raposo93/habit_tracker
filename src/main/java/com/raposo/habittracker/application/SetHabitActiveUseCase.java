package com.raposo.habittracker.application;

import java.util.Objects;

import com.raposo.habittracker.application.habit.HabitNotFoundException;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.HabitId;

public class SetHabitActiveUseCase {
    private final HabitRepository habitRepository;

    public SetHabitActiveUseCase(HabitRepository habitRepository) {
        this.habitRepository = Objects.requireNonNull(habitRepository);
    }

    public void execute(HabitId habitId, boolean active) {
        Objects.requireNonNull(habitId);
        if (!habitRepository.setActive(habitId, active)) {
            throw new HabitNotFoundException(habitId);
        }
    }
}
