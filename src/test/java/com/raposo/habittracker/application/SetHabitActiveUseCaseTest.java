package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.raposo.habittracker.application.habit.HabitNotFoundException;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.HabitId;

class SetHabitActiveUseCaseTest {
    private final HabitRepository repository = mock(HabitRepository.class);
    private final SetHabitActiveUseCase useCase = new SetHabitActiveUseCase(repository);

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenExistingHabitWhenExecuteThenSetDesiredState(boolean active) {
        HabitId id = HabitId.of("sleep");
        given(repository.setActive(id, active)).willReturn(true);
        useCase.execute(id, active);
        verify(repository).setActive(id, active);
    }

    @Test
    void givenMissingHabitWhenExecuteThenThrowNotFound() {
        assertThrows(HabitNotFoundException.class,
                () -> useCase.execute(HabitId.of("missing"), false));
    }
}
