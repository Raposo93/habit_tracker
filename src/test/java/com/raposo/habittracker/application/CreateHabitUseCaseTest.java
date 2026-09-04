package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.raposo.habittracker.application.habit.CreateHabitInput;
import com.raposo.habittracker.application.habit.HabitNameAlreadyExistsException;
import com.raposo.habittracker.application.habit.InvalidHabitCadenceException;
import com.raposo.habittracker.application.habit.InvalidHabitNameException;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;

class CreateHabitUseCaseTest {

    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final CreateHabitUseCase useCase = new CreateHabitUseCase(habitRepository);

    @Test
    void givenValidInputWhenExecuteThenPersistAndReturnSameActiveHabit() {
        given(habitRepository.create(any(Habit.class))).willReturn(true);

        Habit result = useCase.execute(new CreateHabitInput("  Meditation  ", "daily"));

        ArgumentCaptor<Habit> habitCaptor = ArgumentCaptor.forClass(Habit.class);
        verify(habitRepository).create(habitCaptor.capture());

        assertSame(result, habitCaptor.getValue());
        assertEquals("Meditation", result.name());
        assertEquals(HabitCadence.DAILY, result.cadence());
        assertTrue(result.active());
        assertFalse(result.id().value().isBlank());
    }

    @Test
    void givenBlankNameWhenExecuteThenRejectWithoutWriting() {
        InvalidHabitNameException exception = assertThrows(
                InvalidHabitNameException.class,
                () -> useCase.execute(new CreateHabitInput("  ", "DAILY")));

        assertEquals("Habit name cannot be null or blank", exception.getMessage());
        verifyNoInteractions(habitRepository);
    }

    @Test
    void givenMissingCadenceWhenExecuteThenRejectWithoutWriting() {
        InvalidHabitCadenceException exception = assertThrows(
                InvalidHabitCadenceException.class,
                () -> useCase.execute(new CreateHabitInput("Meditation", null)));

        assertEquals("Habit cadence cannot be null or blank", exception.getMessage());
        verifyNoInteractions(habitRepository);
    }

    @Test
    void givenUnknownCadenceWhenExecuteThenRejectWithoutWriting() {
        InvalidHabitCadenceException exception = assertThrows(
                InvalidHabitCadenceException.class,
                () -> useCase.execute(new CreateHabitInput("Meditation", "MONTHLY")));

        assertEquals("Unsupported habit cadence: MONTHLY", exception.getMessage());
        verifyNoInteractions(habitRepository);
    }

    @Test
    void givenExistingNameWhenExecuteThenThrowStableDuplicateError() {
        given(habitRepository.create(any(Habit.class))).willReturn(false);

        HabitNameAlreadyExistsException exception = assertThrows(
                HabitNameAlreadyExistsException.class,
                () -> useCase.execute(new CreateHabitInput("  Meditation  ", "DAILY")));

        assertEquals("Habit name already exists: Meditation", exception.getMessage());
        verify(habitRepository).create(any(Habit.class));
    }
}
