package com.raposo.habittracker.web.habit;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.raposo.habittracker.application.ListHabitsUseCase;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.web.habit.HabitCatalogResponse.HabitResponse;

@WebMvcTest(HabitController.class)
class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListHabitsUseCase listHabitsUseCase;

    @MockitoBean
    private HabitCatalogResponseMapper mapper;

    @Test
    void givenFullCatalogWhenGetHabitsThenReturnActiveAndInactiveHabits() throws Exception {
        List<Habit> habits = List.of(
                Habit.active(HabitId.of("exercise"), "Exercise", HabitCadence.DAILY),
                Habit.inactive(HabitId.of("review"), "Review", HabitCadence.WEEKLY));
        HabitCatalogResponse response = new HabitCatalogResponse(List.of(
                new HabitResponse("exercise", "Exercise", "DAILY", true),
                new HabitResponse("review", "Review", "WEEKLY", false)));

        given(listHabitsUseCase.execute()).willReturn(habits);
        given(mapper.toResponse(habits)).willReturn(response);

        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habits[0].habitId").value("exercise"))
                .andExpect(jsonPath("$.habits[0].habitName").value("Exercise"))
                .andExpect(jsonPath("$.habits[0].cadence").value("DAILY"))
                .andExpect(jsonPath("$.habits[0].active").value(true))
                .andExpect(jsonPath("$.habits[1].habitId").value("review"))
                .andExpect(jsonPath("$.habits[1].habitName").value("Review"))
                .andExpect(jsonPath("$.habits[1].cadence").value("WEEKLY"))
                .andExpect(jsonPath("$.habits[1].active").value(false));
    }
}
