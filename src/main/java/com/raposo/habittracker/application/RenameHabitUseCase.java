package com.raposo.habittracker.application;

import java.util.Objects;

import com.raposo.habittracker.application.habit.HabitNameAlreadyExistsException;
import com.raposo.habittracker.application.habit.HabitNotFoundException;
import com.raposo.habittracker.application.habit.InvalidHabitNameException;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.HabitId;

public class RenameHabitUseCase {
    private final HabitRepository habitRepository;

    public RenameHabitUseCase(HabitRepository habitRepository) {
        this.habitRepository = Objects.requireNonNull(habitRepository);
    }

    public void execute(HabitId habitId, String name) {
        Objects.requireNonNull(habitId);
        if (name == null || name.isBlank()) {
            throw new InvalidHabitNameException();
        }
        String trimmedName = name.trim();
        switch (habitRepository.rename(habitId, trimmedName)) {
            case RENAMED -> { }
            case NOT_FOUND -> throw new HabitNotFoundException(habitId);
            case NAME_ALREADY_EXISTS -> throw new HabitNameAlreadyExistsException(trimmedName);
        }
    }
}
