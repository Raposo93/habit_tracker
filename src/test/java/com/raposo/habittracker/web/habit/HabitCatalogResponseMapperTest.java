package com.raposo.habittracker.web.habit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.web.habit.HabitCatalogResponse.HabitResponse;

class HabitCatalogResponseMapperTest {

    @Test
    void givenActiveAndInactiveHabitsWhenToResponseThenMapManagementFields() {
        List<Habit> habits = List.of(
                Habit.active(HabitId.of("exercise"), "Exercise", HabitCadence.DAILY),
                Habit.inactive(HabitId.of("review"), "Review", HabitCadence.WEEKLY));

        HabitCatalogResponse response = new HabitCatalogResponseMapper().toResponse(habits);

        assertEquals(
                List.of(
                        new HabitResponse("exercise", "Exercise", "DAILY", true),
                        new HabitResponse("review", "Review", "WEEKLY", false)),
                response.habits());
    }
}
