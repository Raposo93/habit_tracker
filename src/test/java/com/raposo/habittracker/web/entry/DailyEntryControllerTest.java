package com.raposo.habittracker.web.entry;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
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
                .andExpect(jsonPath("$.habits[0].entry").value(nullValue()))
                .andExpect(jsonPath("$.habits[1].habitId").value("sleep"))
                .andExpect(jsonPath("$.habits[1].entry.score").value(0.0))
                .andExpect(jsonPath("$.habits[1].entry.note").value("Tired"));
    }

    @Test
    void givenInvalidDateWhenGetContextThenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/entries/context")
                .param("date", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidEntryWhenPostThenCreateAndReturnCreated() throws Exception {
        HabitEntryInput input = input();

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
        return new HabitEntryInput(
                LocalDate.of(2026, 9, 2),
                HabitId.of("sleep"),
                3.0,
                "Rested");
    }
}
