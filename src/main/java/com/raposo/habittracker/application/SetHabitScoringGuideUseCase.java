package com.raposo.habittracker.application;

import java.util.Objects;

import com.raposo.habittracker.application.habit.HabitNotFoundException;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitId;

public class SetHabitScoringGuideUseCase {
    private final HabitRepository habitRepository;

    public SetHabitScoringGuideUseCase(HabitRepository habitRepository) {
        this.habitRepository = Objects.requireNonNull(habitRepository);
    }

    public void execute(HabitId habitId, String scoringGuide) {
        Objects.requireNonNull(habitId);
        if (!habitRepository.setScoringGuide(habitId, Habit.normalizeScoringGuide(scoringGuide))) {
            throw new HabitNotFoundException(habitId);
        }
    }
}
