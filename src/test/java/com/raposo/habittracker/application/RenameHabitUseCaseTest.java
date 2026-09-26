package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.raposo.habittracker.application.habit.HabitNameAlreadyExistsException;
import com.raposo.habittracker.application.habit.HabitNotFoundException;
import com.raposo.habittracker.application.habit.InvalidHabitNameException;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.application.port.HabitRepository.RenameResult;
import com.raposo.habittracker.domain.HabitId;

class RenameHabitUseCaseTest {
    private final HabitRepository repository = mock(HabitRepository.class);
    private final RenameHabitUseCase useCase = new RenameHabitUseCase(repository);
    private final HabitId id = HabitId.of("sleep");

    @Test
    void givenValidNameWhenRenameThenTrimAndPersistByIdentity() {
        given(repository.rename(id, "Rest")).willReturn(RenameResult.RENAMED);
        useCase.execute(id, "  Rest  ");
        verify(repository).rename(id, "Rest");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t\n"})
    void givenBlankNameWhenRenameThenRejectWithoutWriting(String name) {
        assertThrows(InvalidHabitNameException.class, () -> useCase.execute(id, name));
        verifyNoInteractions(repository);
    }

    @Test
    void givenMissingHabitWhenRenameThenThrowNotFound() {
        given(repository.rename(id, "Rest")).willReturn(RenameResult.NOT_FOUND);
        assertThrows(HabitNotFoundException.class, () -> useCase.execute(id, "Rest"));
    }

    @Test
    void givenDuplicateNameWhenRenameThenThrowConflict() {
        given(repository.rename(id, "Rest")).willReturn(RenameResult.NAME_ALREADY_EXISTS);
        assertThrows(HabitNameAlreadyExistsException.class, () -> useCase.execute(id, "Rest"));
    }
}
