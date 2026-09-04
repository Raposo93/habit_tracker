package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;

class ListHabitsUseCaseTest {

    @Test
    void givenActiveAndInactiveHabitsWhenExecuteThenReturnFullCatalog() {
        List<Habit> habits = List.of(
                Habit.active(HabitId.of("exercise"), "Exercise", HabitCadence.DAILY),
                Habit.inactive(HabitId.of("review"), "Review", HabitCadence.WEEKLY));
        HabitRepository habitRepository = mock(HabitRepository.class);
        given(habitRepository.findAll()).willReturn(habits);

        List<Habit> result = new ListHabitsUseCase(habitRepository).execute();

        assertEquals(habits, result);
        verify(habitRepository).findAll();
    }
}
