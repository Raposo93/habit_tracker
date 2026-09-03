package com.raposo.habittracker.web.entry;

import static org.hamcrest.Matchers.nullValue;
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

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.raposo.habittracker.application.CreateHabitEntryUseCase;
import com.raposo.habittracker.application.GetDailyEntryContextUseCase;
import com.raposo.habittracker.application.UpdateHabitEntryUseCase;
import com.raposo.habittracker.application.entry.DailyEntryContext;
import com.raposo.habittracker.application.entry.HabitEntryAlreadyExistsException;
import com.raposo.habittracker.application.entry.HabitEntryInput;
import com.raposo.habittracker.application.entry.HabitEntryNotFoundException;
import com.raposo.habittracker.application.entry.UnknownHabitException;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.web.entry.DailyEntryResponse.EntryResponse;
import com.raposo.habittracker.web.entry.DailyEntryResponse.HabitResponse;

@WebMvcTest(DailyEntryController.class)
class DailyEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetDailyEntryContextUseCase getDailyEntryContextUseCase;

    @MockitoBean
    private CreateHabitEntryUseCase createHabitEntryUseCase;

    @MockitoBean
    private UpdateHabitEntryUseCase updateHabitEntryUseCase;

    @MockitoBean
    private DailyEntryResponseMapper mapper;

    @Test
    void givenMissingAndZeroScoreEntriesWhenGetContextThenSerializeNullAndZeroDifferently() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 2);
        DailyEntryContext context = new DailyEntryContext(date, List.of());
        DailyEntryResponse response = new DailyEntryResponse(
                date,
                List.of(
                        new HabitResponse("exercise", "Exercise", null),
                        new HabitResponse("sleep", "Sleep", new EntryResponse(0.0, "Tired"))));

        given(getDailyEntryContextUseCase.execute(date)).willReturn(context);
        given(mapper.toResponse(context)).willReturn(response);

        mockMvc.perform(get("/api/entries/context")
                .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-09-02"))
                .andExpect(jsonPath("$.habits[0].habitId").value("exercise"))
                .andExpect(jsonPath("$.habits[0].habitName").value("Exercise"))
                .andExpect(jsonPath("$.habits[0].entry").value(nullValue()))
                .andExpect(jsonPath("$.habits[1].habitId").value("sleep"))
                .andExpect(jsonPath("$.habits[1].habitName").value("Sleep"))
                .andExpect(jsonPath("$.habits[1].entry.score").value(0.0))
                .andExpect(jsonPath("$.habits[1].entry.note").value("Tired"));
    }

    @Test
    void givenInvalidDateWhenGetContextThenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/entries/context")
                .param("date", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATE"))
                .andExpect(jsonPath("$.message").value("Date must use YYYY-MM-DD format"));
    }

    @Test
    void givenMissingDateWhenGetContextThenReturnInvalidDate() throws Exception {
        mockMvc.perform(get("/api/entries/context"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATE"));
    }

    @Test
    void givenRetrospectiveEntryWhenPostThenCreateForSelectedDateAndReturnCreated() throws Exception {
        HabitEntryInput input = input(
                LocalDate.of(2020, 1, 15),
                "sleep",
                3.0,
                "Rested");

        mockMvc.perform(post("/api/entries/{date}/{habitId}", input.date(), input.habitId().value())
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "score": 3.0,
                          "note": "Rested"
                        }
                        """))
                .andExpect(status().isCreated());

        verify(createHabitEntryUseCase).execute(input);
    }

    @Test
    void givenOmittedNoteWhenPostThenCreateWithEmptyNote() throws Exception {
        HabitEntryInput input = input(
                LocalDate.of(2026, 9, 2),
                "sleep",
                2.0,
                null);

        mockMvc.perform(post("/api/entries/{date}/{habitId}", input.date(), input.habitId().value())
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "score": 2.0
                        }
                        """))
                .andExpect(status().isCreated());

        verify(createHabitEntryUseCase).execute(input);
    }

    @Test
    void givenOutOfRangeScoreWhenPostThenReturnInvalidEntry() throws Exception {
        mockMvc.perform(post("/api/entries/{date}/{habitId}", "2026-09-02", "sleep")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "score": 4.0
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SCORE"))
                .andExpect(jsonPath("$.message").value("Score must be between 0 and 3"));

        verifyNoInteractions(createHabitEntryUseCase);
    }

    @Test
    void givenMissingScoreWhenPostThenReturnInvalidEntry() throws Exception {
        mockMvc.perform(post("/api/entries/{date}/{habitId}", "2026-09-02", "sleep")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "note": "Rested"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SCORE"))
                .andExpect(jsonPath("$.message").value("Score cannot be null"));

        verifyNoInteractions(createHabitEntryUseCase);
    }

    @Test
    void givenUnknownHabitWhenPostThenReturnNotFound() throws Exception {
        HabitEntryInput input = input(
                LocalDate.of(2026, 9, 2),
                "unknown",
                3.0,
                "Rested");
        UnknownHabitException exception = new UnknownHabitException(input.habitId());
        willThrow(exception).given(createHabitEntryUseCase).execute(input);

        mockMvc.perform(post("/api/entries/{date}/{habitId}", input.date(), input.habitId().value())
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "score": 3.0,
                          "note": "Rested"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_HABIT"))
                .andExpect(jsonPath("$.message").value(exception.getMessage()));
    }

    @Test
    void givenInvalidDateWhenPostThenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/entries/{date}/{habitId}", "not-a-date", "sleep")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "score": 3.0,
                          "note": "Rested"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATE"))
                .andExpect(jsonPath("$.message").value("Date must use YYYY-MM-DD format"));

        verifyNoInteractions(createHabitEntryUseCase);
    }

    @Test
    void givenValidCorrectionWhenPutThenUpdateAndReturnNoContent() throws Exception {
        HabitEntryInput input = input();

        mockMvc.perform(put("/api/entries/{date}/{habitId}", input.date(), input.habitId().value())
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "score": 3.0,
                          "note": "Rested"
                        }
                        """))
                .andExpect(status().isNoContent());

        verify(updateHabitEntryUseCase).execute(input);
    }

    @Test
    void givenExistingEntryWhenPostThenReturnConflict() throws Exception {
        HabitEntryInput input = input();
        HabitEntryAlreadyExistsException exception = new HabitEntryAlreadyExistsException(
                input.date(),
                input.habitId());
        willThrow(exception).given(createHabitEntryUseCase).execute(input);

        mockMvc.perform(post("/api/entries/{date}/{habitId}", input.date(), input.habitId().value())
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "score": 3.0,
                          "note": "Rested"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ENTRY_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value(exception.getMessage()));
    }

    @Test
    void givenMissingEntryWhenPutThenReturnNotFound() throws Exception {
        HabitEntryInput input = input();
        HabitEntryNotFoundException exception = new HabitEntryNotFoundException(
                input.date(),
                input.habitId());
        willThrow(exception).given(updateHabitEntryUseCase).execute(input);

        mockMvc.perform(put("/api/entries/{date}/{habitId}", input.date(), input.habitId().value())
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "score": 3.0,
                          "note": "Rested"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ENTRY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(exception.getMessage()));
    }

    private static HabitEntryInput input() {
        return input(
                LocalDate.of(2026, 9, 2),
                "sleep",
                3.0,
                "Rested");
    }

    private static HabitEntryInput input(
            LocalDate date,
            String habitId,
            double score,
            String note) {
        return new HabitEntryInput(
                date,
                HabitId.of(habitId),
                score,
                note);
    }
}
