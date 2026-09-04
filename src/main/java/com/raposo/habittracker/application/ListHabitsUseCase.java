package com.raposo.habittracker.application;

import java.util.List;
import java.util.Objects;

import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.Habit;

public class ListHabitsUseCase {

    private final HabitRepository habitRepository;

    public ListHabitsUseCase(HabitRepository habitRepository) {
        this.habitRepository = Objects.requireNonNull(habitRepository);
    }

    public List<Habit> execute() {
        return habitRepository.findAll();
    }
}
