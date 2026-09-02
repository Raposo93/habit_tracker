package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.entry.HabitEntryInput;
import com.raposo.habittracker.application.entry.HabitEntryNotFoundException;
import com.raposo.habittracker.application.entry.UnknownHabitException;
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
    void givenKnownHabitAndExistingEntryWhenExecuteThenUpdateScoreAndNote() {
        HabitEntryInput input = input(3.0, "Corrected");
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
    void givenExistingEntryWhenExecuteWithOmittedNoteThenRemoveStoredNote() {
        HabitEntryInput input = input(2.0, null);
        given(habitRepository.findById(input.habitId())).willReturn(Optional.of(habit(input.habitId())));
        given(entryRepository.updateEntry(
                input.date(),
                input.habitId(),
                new StoredEntry(2.0, "")))
                .willReturn(true);

        useCase.execute(input);

        verify(entryRepository).updateEntry(
                input.date(),
                input.habitId(),
                new StoredEntry(2.0, ""));
    }

    @Test
    void givenMissingEntryWhenExecuteThenThrowNotFoundWithoutCreatingEntry() {
        HabitEntryInput input = input(3.0, "Corrected");
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
        verify(entryRepository, never()).createEntry(any(), any(), any());
    }

    @Test
    void givenUnknownHabitWhenExecuteThenDoNotWrite() {
        HabitEntryInput input = input(3.0, "Corrected");
        given(habitRepository.findById(input.habitId())).willReturn(Optional.empty());

        UnknownHabitException exception = assertThrows(
                UnknownHabitException.class,
                () -> useCase.execute(input));

        assertEquals("Unknown habit: sleep", exception.getMessage());
        verifyNoInteractions(entryRepository);
    }

    private static HabitEntryInput input(double score, String note) {
        return new HabitEntryInput(
                LocalDate.of(2026, 9, 2),
                HabitId.of("sleep"),
                score,
                note);
    }

    private static Habit habit(HabitId habitId) {
        return Habit.active(habitId, "Sleep", HabitCadence.DAILY);
    }
}
