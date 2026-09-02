package com.raposo.habittracker.web.entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.raposo.habittracker.application.entry.DailyEntryContext;
import com.raposo.habittracker.application.entry.HabitEntryContext;
import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;
import com.raposo.habittracker.web.entry.DailyEntryResponse.HabitResponse;

class DailyEntryResponseMapperTest {

    @Test
    void givenMissingAndZeroScoreEntriesWhenToResponseThenPreserveTheirDifferentSemantics() {
        DailyEntryContext context = new DailyEntryContext(
                LocalDate.of(2026, 9, 2),
                List.of(
                        new HabitEntryContext(
                                HabitId.of("exercise"),
                                "Exercise",
                                Optional.empty()),
                        new HabitEntryContext(
                                HabitId.of("sleep"),
                                "Sleep",
                                Optional.of(new StoredEntry(0.0, "Tired")))));

        DailyEntryResponse response = new DailyEntryResponseMapper().toResponse(context);

        HabitResponse missingEntryHabit = response.habits().get(0);
        HabitResponse zeroScoreHabit = response.habits().get(1);

        assertNull(missingEntryHabit.entry());
        assertEquals("sleep", zeroScoreHabit.habitId());
        assertEquals("Sleep", zeroScoreHabit.habitName());
        assertEquals(0.0, zeroScoreHabit.entry().score());
        assertEquals("Tired", zeroScoreHabit.entry().note());
    }
}
