package com.raposo.habittracker.application;

import java.util.Locale;
import java.util.Objects;

import com.raposo.habittracker.application.habit.CreateHabitInput;
import com.raposo.habittracker.application.habit.HabitNameAlreadyExistsException;
import com.raposo.habittracker.application.habit.InvalidHabitCadenceException;
import com.raposo.habittracker.application.habit.InvalidHabitNameException;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;

public class CreateHabitUseCase {

    private final HabitRepository habitRepository;

    public CreateHabitUseCase(HabitRepository habitRepository) {
        this.habitRepository = Objects.requireNonNull(habitRepository);
    }

    public Habit execute(CreateHabitInput input) {
        Objects.requireNonNull(input);

        String name = validateName(input.name());
        HabitCadence cadence = parseCadence(input.cadence());
        Habit habit = Habit.active(HabitId.generate(), name, cadence);

        if (!habitRepository.create(habit)) {
            throw new HabitNameAlreadyExistsException(name);
        }

        return habit;
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidHabitNameException();
        }

        return name.trim();
    }

    private HabitCadence parseCadence(String cadence) {
        if (cadence == null || cadence.isBlank()) {
            throw InvalidHabitCadenceException.missing();
        }

        String normalizedCadence = cadence.trim().toUpperCase(Locale.ROOT);

        try {
            return HabitCadence.valueOf(normalizedCadence);
        } catch (IllegalArgumentException exception) {
            throw InvalidHabitCadenceException.unsupported(cadence);
        }
    }
}
