package com.raposo.habittracker.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.habit.HabitCatalogChangedException;
import com.raposo.habittracker.application.habit.InvalidHabitOrderException;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.HabitId;

class ReorderHabitsUseCaseTest {
    private final HabitRepository repository = mock(HabitRepository.class);
    private final ReorderHabitsUseCase useCase = new ReorderHabitsUseCase(repository);

    @Test
    void givenValidOrderWhenExecuteThenPreserveRequestedSequence() {
        List<HabitId> ids = List.of(HabitId.of("review"), HabitId.of("sleep"));
        given(repository.reorder(ids)).willReturn(true);
        useCase.execute(List.of("review", "sleep"));
        verify(repository).reorder(ids);
    }

    @Test
    void givenInvalidIdentitiesWhenExecuteThenRejectBeforeWriting() {
        assertThrows(InvalidHabitOrderException.class, () -> useCase.execute(null));
        for (List<String> ids : List.of(Arrays.asList("sleep", null), List.of(" "),
                List.of("sleep", "sleep"), List.of("sleep", " sleep "))) {
            assertThrows(InvalidHabitOrderException.class, () -> useCase.execute(ids));
        }
        verifyNoInteractions(repository);
    }

    @Test
    void givenMismatchedCatalogWhenExecuteThenRejectWithConflict() {
        assertThrows(HabitCatalogChangedException.class, () -> useCase.execute(List.of("missing")));
    }

    @Test
    void givenEmptyCatalogWhenExecuteThenAllowEmptyOrder() {
        given(repository.reorder(List.of())).willReturn(true);
        useCase.execute(List.of());
        verify(repository).reorder(List.of());
    }
}
