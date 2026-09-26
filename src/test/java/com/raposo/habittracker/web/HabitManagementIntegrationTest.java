package com.raposo.habittracker.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.infrastructure.sqlite.SqliteHabitEntryRepository;
import com.raposo.habittracker.infrastructure.sqlite.SqliteHabitRepository;
import com.raposo.habittracker.web.config.PersistenceConfiguration;

import tools.jackson.databind.ObjectMapper;

class HabitManagementIntegrationTest {

    @TempDir
    Path tempDir;

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void startApplicationWithIsolatedStorage() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource(
                "test-storage", Map.of("test.database", tempDir.resolve("habits.db").toString())));
        context.register(TestWebConfiguration.class);
        context.refresh();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void closeApplication() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void givenNewHabitWhenCreatedAndRecordedThenDailyEntryImmediatelyShowsStoredValues() throws Exception {
        String habitId = createHabit();

        dailyContext("2026-09-02")
                .andExpect(jsonPath("$.habits.length()").value(1))
                .andExpect(jsonPath("$.habits[0].habitId").value(habitId))
                .andExpect(jsonPath("$.habits[0].habitName").value("Sleep"))
                .andExpect(jsonPath("$.habits[0].entry").isEmpty());

        createEntry(habitId, "2026-09-02", "{\"score\":0,\"note\":\"Tired\"}");

        dailyContext("2026-09-02")
                .andExpect(jsonPath("$.habits[0].habitId").value(habitId))
                .andExpect(jsonPath("$.habits[0].entry.score").value(0.0))
                .andExpect(jsonPath("$.habits[0].entry.note").value("Tired"));
        dailyContext("2026-09-03")
                .andExpect(jsonPath("$.habits[0].entry").isEmpty());

        report("2026-09-02", "2026-09-02")
                .andExpect(jsonPath("$.summary[0].trend").value("NO_BASELINE"))
                .andExpect(jsonPath("$.summary[0].previousPeriodScore").isEmpty())
                .andExpect(jsonPath("$.summary[0].delta").isEmpty())
                .andExpect(jsonPath("$.summary[0].currentRecordedDays").value(1))
                .andExpect(jsonPath("$.summary[0].currentMissingDays").value(0));
    }

    @Test
    void givenHistoricalEntryWhenDeactivateAndReactivateThenHistoryAndIdentityRemainIntact() throws Exception {
        String habitId = createHabit();
        createEntry(habitId, "2026-09-02", "{\"score\":0,\"note\":\"Tired\"}");

        setActive(habitId, false);

        dailyContext("2026-09-02").andExpect(jsonPath("$.habits").isEmpty());
        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habits.length()").value(1))
                .andExpect(jsonPath("$.habits[0].habitId").value(habitId))
                .andExpect(jsonPath("$.habits[0].active").value(false));
        report("2026-09-02", "2026-09-02")
                .andExpect(jsonPath("$.entries.length()").value(1))
                .andExpect(jsonPath("$.entries[0].habit").value("Sleep"))
                .andExpect(jsonPath("$.entries[0].score").value(0.0))
                .andExpect(jsonPath("$.entries[0].note").value("Tired"))
                .andExpect(jsonPath("$.summary[0].currentRecordedDays").value(1));

        setActive(habitId, true);

        dailyContext("2026-09-02")
                .andExpect(jsonPath("$.habits.length()").value(1))
                .andExpect(jsonPath("$.habits[0].habitId").value(habitId))
                .andExpect(jsonPath("$.habits[0].entry.score").value(0.0))
                .andExpect(jsonPath("$.habits[0].entry.note").value("Tired"));
        report("2026-09-02", "2026-09-02")
                .andExpect(jsonPath("$.entries.length()").value(1))
                .andExpect(jsonPath("$.entries[0].score").value(0.0))
                .andExpect(jsonPath("$.entries[0].note").value("Tired"));
    }

    @Test
    void givenTwoPeriodsWhenRenameThenReadsAndCorrectionsUseSameIdentityWithCurrentName() throws Exception {
        String habitId = createHabit();
        createEntry(habitId, "2026-09-01", "{\"score\":2,\"note\":\"Rested\"}");
        createEntry(habitId, "2026-09-02", "{\"score\":0,\"note\":\"Tired\"}");

        mockMvc.perform(put("/api/habits/{habitId}/name", habitId)
                .contentType(APPLICATION_JSON).content("{\"habitName\":\"Rest\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habits.length()").value(1))
                .andExpect(jsonPath("$.habits[0].habitId").value(habitId))
                .andExpect(jsonPath("$.habits[0].habitName").value("Rest"));
        dailyContext("2026-09-02")
                .andExpect(jsonPath("$.habits[0].habitId").value(habitId))
                .andExpect(jsonPath("$.habits[0].habitName").value("Rest"))
                .andExpect(jsonPath("$.habits[0].entry.score").value(0.0))
                .andExpect(jsonPath("$.habits[0].entry.note").value("Tired"));
        report("2026-09-02", "2026-09-02")
                .andExpect(jsonPath("$.entries[0].habit").value("Rest"))
                .andExpect(jsonPath("$.entries[0].score").value(0.0))
                .andExpect(jsonPath("$.entries[0].note").value("Tired"))
                .andExpect(jsonPath("$.summary.length()").value(1))
                .andExpect(jsonPath("$.summary[0].habit").value("Rest"))
                .andExpect(jsonPath("$.summary[0].previousPeriodScore").value(2.0))
                .andExpect(jsonPath("$.summary[0].currentPeriodScore").value(0.0))
                .andExpect(jsonPath("$.summary[0].delta").value(-2.0))
                .andExpect(jsonPath("$.summary[0].trend").value("WORSENED"));
        report("2026-09-01", "2026-09-01")
                .andExpect(jsonPath("$.entries[0].habit").value("Rest"))
                .andExpect(jsonPath("$.entries[0].score").value(2.0))
                .andExpect(jsonPath("$.entries[0].note").value("Rested"));

        mockMvc.perform(put("/api/entries/{date}/{habitId}", "2026-09-02", habitId)
                .contentType(APPLICATION_JSON).content("{\"score\":3,\"note\":null}"))
                .andExpect(status().isNoContent());

        dailyContext("2026-09-02")
                .andExpect(jsonPath("$.habits[0].habitId").value(habitId))
                .andExpect(jsonPath("$.habits[0].entry.score").value(3.0))
                .andExpect(jsonPath("$.habits[0].entry.note").isEmpty());
        report("2026-09-02", "2026-09-02")
                .andExpect(jsonPath("$.summary[0].previousPeriodScore").value(2.0))
                .andExpect(jsonPath("$.summary[0].currentPeriodScore").value(3.0))
                .andExpect(jsonPath("$.summary[0].delta").value(1.0))
                .andExpect(jsonPath("$.summary[0].trend").value("IMPROVED"));
    }

    private String createHabit() throws Exception {
        String response = mockMvc.perform(post("/api/habits")
                .contentType(APPLICATION_JSON)
                .content("{\"habitName\":\"Sleep\",\"cadence\":\"DAILY\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();
        String habitId = new ObjectMapper().readTree(response).get("habitId").asString();
        assertFalse(habitId.isBlank());
        return habitId;
    }

    private void createEntry(String habitId, String date, String body) throws Exception {
        mockMvc.perform(post("/api/entries/{date}/{habitId}", date, habitId)
                .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    private void setActive(String habitId, boolean active) throws Exception {
        mockMvc.perform(put("/api/habits/{habitId}/active", habitId)
                .contentType(APPLICATION_JSON).content("{\"active\":" + active + "}"))
                .andExpect(status().isNoContent());
    }

    private ResultActions dailyContext(String date) throws Exception {
        return mockMvc.perform(get("/api/entries/context").param("date", date))
                .andExpect(status().isOk());
    }

    private ResultActions report(String start, String end) throws Exception {
        return mockMvc.perform(get("/api/reports").param("startDate", start).param("endDate", end))
                .andExpect(status().isOk());
    }

    // Use production web wiring; replace only storage configuration to isolate each scenario.
    @TestConfiguration
    @EnableWebMvc
    @ComponentScan(basePackages = "com.raposo.habittracker.web", excludeFilters =
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PersistenceConfiguration.class))
    static class TestWebConfiguration {
        @Bean
        HabitEntryRepository habitEntryRepository(@Value("${test.database}") String database) {
            return new SqliteHabitEntryRepository(Path.of(database));
        }

        @Bean
        HabitRepository habitRepository(@Value("${test.database}") String database) {
            return new SqliteHabitRepository(Path.of(database));
        }
    }
}
