package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import com.raposo.habittracker.application.habit.HabitNotFoundException;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;

class SetHabitScoringGuideUseCaseTest {
    @Test
    void preservesMultilineTextAndNormalizesAbsentGuides() {
        HabitId id = HabitId.of("sleep");
        String guide = " 0: tired\n3: rested ";
        assertEquals(guide, new Habit(id, "Sleep", HabitCadence.DAILY, true, guide).scoringGuide());
        HabitRepository repository = mock(HabitRepository.class);
        when(repository.setScoringGuide(eq(id), nullable(String.class))).thenReturn(true);
        var useCase = new SetHabitScoringGuideUseCase(repository);
        useCase.execute(id, guide);
        verify(repository).setScoringGuide(id, guide);
        useCase.execute(id, " \n ");
        useCase.execute(id, null);
        verify(repository, times(2)).setScoringGuide(id, null);
        assertNull(new Habit(id, "Sleep", HabitCadence.DAILY, true, "\n ").scoringGuide());
    }

    @Test
    void missingHabitDoesNotCreateOne() {
        var useCase = new SetHabitScoringGuideUseCase(mock(HabitRepository.class));
        assertThrows(HabitNotFoundException.class, () -> useCase.execute(HabitId.of("missing"), "Guide"));
    }
}
