package com.raposo.habittracker.web.habit;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.raposo.habittracker.application.CreateHabitUseCase;
import com.raposo.habittracker.application.ListHabitsUseCase;
import com.raposo.habittracker.application.RenameHabitUseCase;
import com.raposo.habittracker.application.ReorderHabitsUseCase;
import com.raposo.habittracker.application.SetHabitActiveUseCase;
import com.raposo.habittracker.application.SetHabitScoringGuideUseCase;
import com.raposo.habittracker.application.habit.CreateHabitInput;
import com.raposo.habittracker.application.habit.HabitCatalogChangedException;
import com.raposo.habittracker.application.habit.HabitNameAlreadyExistsException;
import com.raposo.habittracker.application.habit.HabitNotFoundException;
import com.raposo.habittracker.application.habit.InvalidHabitCadenceException;
import com.raposo.habittracker.application.habit.InvalidHabitNameException;
import com.raposo.habittracker.application.habit.InvalidHabitOrderException;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitCadence;
import com.raposo.habittracker.domain.HabitId;

@WebMvcTest(HabitController.class)
class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListHabitsUseCase listHabitsUseCase;

    @MockitoBean
    private CreateHabitUseCase createHabitUseCase;

    @MockitoBean
    private HabitResponseMapper mapper;

    @MockitoBean
    private SetHabitActiveUseCase setHabitActiveUseCase;

    @MockitoBean
    private RenameHabitUseCase renameHabitUseCase;

    @MockitoBean
    private ReorderHabitsUseCase reorderHabitsUseCase;

    @MockitoBean
    private SetHabitScoringGuideUseCase setHabitScoringGuideUseCase;

    @Test
    void givenFullCatalogWhenGetHabitsThenReturnActiveAndInactiveHabits() throws Exception {
        List<Habit> habits = List.of(
                Habit.active(HabitId.of("exercise"), "Exercise", HabitCadence.DAILY),
                Habit.inactive(HabitId.of("review"), "Review", HabitCadence.WEEKLY));
        HabitCatalogResponse response = new HabitCatalogResponse(List.of(
                new HabitResponse("exercise", "Exercise", "DAILY", true),
                new HabitResponse("review", "Review", "WEEKLY", false)));

        given(listHabitsUseCase.execute()).willReturn(habits);
        given(mapper.toCatalogResponse(habits)).willReturn(response);

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

    @Test
    void givenValidHabitWhenPostThenCreateAndReturnIt() throws Exception {
        CreateHabitInput input = new CreateHabitInput("Meditation", "DAILY");
        Habit habit = Habit.active(
                HabitId.of("generated-id"),
                "Meditation",
                HabitCadence.DAILY);
        HabitResponse response = new HabitResponse(
                "generated-id",
                "Meditation",
                "DAILY",
                true);
        given(createHabitUseCase.execute(input)).willReturn(habit);
        given(mapper.toResponse(habit)).willReturn(response);

        mockMvc.perform(post("/api/habits")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "habitName": "Meditation",
                          "cadence": "DAILY"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.habitId").value("generated-id"))
                .andExpect(jsonPath("$.habitName").value("Meditation"))
                .andExpect(jsonPath("$.cadence").value("DAILY"))
                .andExpect(jsonPath("$.active").value(true));

        verify(createHabitUseCase).execute(input);
        verify(mapper).toResponse(habit);
    }

    @Test
    void givenBlankNameWhenPostThenReturnStableInvalidNameError() throws Exception {
        CreateHabitInput input = new CreateHabitInput("  ", "DAILY");
        InvalidHabitNameException exception = new InvalidHabitNameException();
        willThrow(exception).given(createHabitUseCase).execute(input);

        mockMvc.perform(post("/api/habits")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "habitName": "  ",
                          "cadence": "DAILY"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_HABIT_NAME"))
                .andExpect(jsonPath("$.message").value(exception.getMessage()));
    }

    @Test
    void givenMissingCadenceWhenPostThenReturnStableInvalidCadenceError() throws Exception {
        CreateHabitInput input = new CreateHabitInput("Meditation", null);
        InvalidHabitCadenceException exception = InvalidHabitCadenceException.missing();
        willThrow(exception).given(createHabitUseCase).execute(input);

        mockMvc.perform(post("/api/habits")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "habitName": "Meditation"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_HABIT_CADENCE"))
                .andExpect(jsonPath("$.message").value(exception.getMessage()));
    }

    @Test
    void givenUnknownCadenceWhenPostThenReturnStableInvalidCadenceError() throws Exception {
        CreateHabitInput input = new CreateHabitInput("Meditation", "MONTHLY");
        InvalidHabitCadenceException exception = InvalidHabitCadenceException.unsupported("MONTHLY");
        willThrow(exception).given(createHabitUseCase).execute(input);

        mockMvc.perform(post("/api/habits")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "habitName": "Meditation",
                          "cadence": "MONTHLY"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_HABIT_CADENCE"))
                .andExpect(jsonPath("$.message").value(exception.getMessage()));
    }

    @Test
    void givenExistingNameWhenPostThenReturnConflict() throws Exception {
        CreateHabitInput input = new CreateHabitInput("Meditation", "DAILY");
        HabitNameAlreadyExistsException exception = new HabitNameAlreadyExistsException("Meditation");
        willThrow(exception).given(createHabitUseCase).execute(input);

        mockMvc.perform(post("/api/habits")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "habitName": "Meditation",
                          "cadence": "DAILY"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("HABIT_NAME_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value(exception.getMessage()));
    }

    @Test
    void givenMalformedJsonWhenPostThenReturnStableInvalidHabitError() throws Exception {
        mockMvc.perform(post("/api/habits")
                .contentType(APPLICATION_JSON)
                .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_HABIT"))
                .andExpect(jsonPath("$.message").value("Habit request must contain valid JSON"));

        verifyNoInteractions(createHabitUseCase);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenDesiredStateWhenPutThenReturnNoContent(boolean active) throws Exception {
        mockMvc.perform(put("/api/habits/sleep/active")
                .contentType(APPLICATION_JSON)
                .content("{\"active\":" + active + "}"))
                .andExpect(status().isNoContent());
        verify(setHabitActiveUseCase).execute(HabitId.of("sleep"), active);
    }

    @Test
    void givenMissingHabitWhenPutThenReturnNotFound() throws Exception {
        willThrow(new HabitNotFoundException(HabitId.of("missing")))
                .given(setHabitActiveUseCase).execute(HabitId.of("missing"), false);
        mockMvc.perform(put("/api/habits/missing/active")
                .contentType(APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("HABIT_NOT_FOUND"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"active\":null}", "{", "{\"active\":[]}"})
    void givenInvalidStateRequestWhenPutThenRejectWithoutWriting(String body) throws Exception {
        mockMvc.perform(put("/api/habits/sleep/active")
                .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_HABIT"));
        verifyNoInteractions(setHabitActiveUseCase);
    }

    @Test
    void givenNewNameWhenPutThenRenameAndReturnNoContent() throws Exception {
        mockMvc.perform(put("/api/habits/sleep/name")
                .contentType(APPLICATION_JSON).content("{\"habitName\":\"Rest\"}"))
                .andExpect(status().isNoContent());
        verify(renameHabitUseCase).execute(HabitId.of("sleep"), "Rest");
    }

    @Test
    void givenMissingHabitWhenRenameThenReturnStableNotFound() throws Exception {
        willThrow(new HabitNotFoundException(HabitId.of("missing")))
                .given(renameHabitUseCase).execute(HabitId.of("missing"), "Rest");
        mockMvc.perform(put("/api/habits/missing/name")
                .contentType(APPLICATION_JSON).content("{\"habitName\":\"Rest\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("HABIT_NOT_FOUND"));
    }

    @Test
    void givenDuplicateNameWhenRenameThenReturnStableConflict() throws Exception {
        willThrow(new HabitNameAlreadyExistsException("Rest"))
                .given(renameHabitUseCase).execute(HabitId.of("sleep"), "Rest");
        mockMvc.perform(put("/api/habits/sleep/name")
                .contentType(APPLICATION_JSON).content("{\"habitName\":\"Rest\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("HABIT_NAME_ALREADY_EXISTS"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"habitName\":null}", "{\"habitName\":\"\"}", "{\"habitName\":\"  \"}"})
    void givenBlankNameWhenRenameThenReturnStableInvalidName(String body) throws Exception {
        willThrow(new InvalidHabitNameException()).given(renameHabitUseCase)
                .execute(org.mockito.ArgumentMatchers.eq(HabitId.of("sleep")),
                        org.mockito.ArgumentMatchers.any());
        mockMvc.perform(put("/api/habits/sleep/name")
                .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_HABIT_NAME"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"habitName\":[]}"})
    void givenMalformedRenameRequestWhenPutThenRejectWithoutCallingUseCase(String body) throws Exception {
        mockMvc.perform(put("/api/habits/sleep/name")
                .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_HABIT"));
        verifyNoInteractions(renameHabitUseCase);
    }

    @Test
    void givenCompleteOrderWhenPutThenAdaptToUseCase() throws Exception {
        mockMvc.perform(put("/api/habits/order").contentType(APPLICATION_JSON)
                .content("{\"habitIds\":[\"review\",\"sleep\"]}"))
                .andExpect(status().isNoContent());
        verify(reorderHabitsUseCase).execute(List.of("review", "sleep"));
    }

    @Test
    void givenInvalidOrderWhenPutThenReturnStableBadRequest() throws Exception {
        willThrow(new InvalidHabitOrderException()).given(reorderHabitsUseCase).execute(null);
        mockMvc.perform(put("/api/habits/order").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_HABIT_ORDER"));
    }

    @Test
    void givenChangedCatalogWhenPutThenReturnConflict() throws Exception {
        willThrow(new HabitCatalogChangedException()).given(reorderHabitsUseCase).execute(List.of("sleep"));
        mockMvc.perform(put("/api/habits/order").contentType(APPLICATION_JSON)
                .content("{\"habitIds\":[\"sleep\"]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("HABIT_CATALOG_CHANGED"));
    }
}
