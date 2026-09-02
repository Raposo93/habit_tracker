package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.entry.HabitEntryAlreadyExistsException;
import com.raposo.habittracker.application.entry.HabitEntryInput;
import com.raposo.habittracker.application.entry.UnknownHabitException;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;

class CreateHabitEntryUseCaseTest {

    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final HabitEntryRepository entryRepository = mock(HabitEntryRepository.class);
    private final CreateHabitEntryUseCase useCase = new CreateHabitEntryUseCase(
            habitRepository,
            entryRepository);

    @Test
    void givenKnownHabitAndMissingEntryWhenExecuteThenCreateEntry() {
        HabitEntryInput input = input();
        given(habitRepository.findById(input.habitId())).willReturn(Optional.of(habit(input.habitId())));
        given(entryRepository.createEntry(
                input.date(),
                input.habitId(),
                new StoredEntry(input.score(), input.note())))
                .willReturn(true);

        useCase.execute(input);

        verify(entryRepository).createEntry(
                input.date(),
                input.habitId(),
                new StoredEntry(input.score(), input.note()));
    }

    @Test
    void givenExistingEntryWhenExecuteThenThrowAlreadyExists() {
        HabitEntryInput input = input();
        given(habitRepository.findById(input.habitId())).willReturn(Optional.of(habit(input.habitId())));
        given(entryRepository.createEntry(
                input.date(),
                input.habitId(),
                new StoredEntry(input.score(), input.note())))
                .willReturn(false);

        HabitEntryAlreadyExistsException exception = assertThrows(
                HabitEntryAlreadyExistsException.class,
                () -> useCase.execute(input));

        assertEquals(
                "Entry already exists for habit sleep on 2026-09-02",
                exception.getMessage());
    }

    @Test
    void givenUnknownHabitWhenExecuteThenDoNotWrite() {
        HabitEntryInput input = input();
        given(habitRepository.findById(input.habitId())).willReturn(Optional.empty());

        UnknownHabitException exception = assertThrows(
                UnknownHabitException.class,
                () -> useCase.execute(input));

        assertEquals("Unknown habit: sleep", exception.getMessage());
        verifyNoInteractions(entryRepository);
    }

    private static HabitEntryInput input() {
        return new HabitEntryInput(
                LocalDate.of(2026, 9, 2),
                HabitId.of("sleep"),
                3.0,
                "Rested");
    }

    private static Habit habit(HabitId habitId) {
        return Habit.active(habitId, "Sleep", HabitCadence.DAILY);
    }
}
