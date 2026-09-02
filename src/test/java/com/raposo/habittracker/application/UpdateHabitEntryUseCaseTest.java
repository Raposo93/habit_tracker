package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.entry.HabitEntryInput;
import com.raposo.habittracker.application.entry.HabitEntryNotFoundException;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;

class UpdateHabitEntryUseCaseTest {

    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final HabitEntryRepository entryRepository = mock(HabitEntryRepository.class);
    private final UpdateHabitEntryUseCase useCase = new UpdateHabitEntryUseCase(
            habitRepository,
            entryRepository);

    @Test
    void givenKnownHabitAndExistingEntryWhenExecuteThenUpdateEntry() {
        HabitEntryInput input = input();
        given(habitRepository.findById(input.habitId())).willReturn(Optional.of(habit(input.habitId())));
        given(entryRepository.updateEntry(
                input.date(),
                input.habitId(),
                new StoredEntry(input.score(), input.note())))
                .willReturn(true);

        useCase.execute(input);

        verify(entryRepository).updateEntry(
                input.date(),
                input.habitId(),
                new StoredEntry(input.score(), input.note()));
    }

    @Test
    void givenMissingEntryWhenExecuteThenThrowNotFound() {
        HabitEntryInput input = input();
        given(habitRepository.findById(input.habitId())).willReturn(Optional.of(habit(input.habitId())));
        given(entryRepository.updateEntry(
                input.date(),
                input.habitId(),
                new StoredEntry(input.score(), input.note())))
                .willReturn(false);

        HabitEntryNotFoundException exception = assertThrows(
                HabitEntryNotFoundException.class,
                () -> useCase.execute(input));

        assertEquals(
                "Entry not found for habit sleep on 2026-09-02",
                exception.getMessage());
    }

    private static HabitEntryInput input() {
        return new HabitEntryInput(
                LocalDate.of(2026, 9, 2),
                HabitId.of("sleep"),
                3.0,
                "Corrected");
    }

    private static Habit habit(HabitId habitId) {
        return Habit.active(habitId, "Sleep", HabitCadence.DAILY);
    }
}
