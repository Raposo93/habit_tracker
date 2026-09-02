package com.raposo.habittracker.application.entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.raposo.habittracker.domain.HabitId;

class HabitEntryInputTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 2);
    private static final HabitId HABIT_ID = HabitId.of("sleep");

    @Test
    void givenNullDateWhenCreateThenRejectInput() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new HabitEntryInput(null, HABIT_ID, 2.0, ""));

        assertEquals("Entry date cannot be null", exception.getMessage());
    }

    @Test
    void givenNullHabitIdWhenCreateThenRejectInput() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new HabitEntryInput(DATE, null, 2.0, ""));

        assertEquals("HabitId cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidScores")
    void givenInvalidScoreWhenCreateThenRejectInput(double score) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new HabitEntryInput(DATE, HABIT_ID, score, ""));

        assertEquals("Score must be between 0 and 3", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0.0, 3.0 })
    void givenBoundaryScoreWhenCreateThenAcceptInput(double score) {
        HabitEntryInput input = new HabitEntryInput(DATE, HABIT_ID, score, "");

        assertEquals(score, input.score());
    }

    @Test
    void givenNullNoteWhenCreateThenNormalizeToEmptyNote() {
        HabitEntryInput input = new HabitEntryInput(DATE, HABIT_ID, 2.0, null);

        assertEquals("", input.note());
    }

    private static Stream<Double> invalidScores() {
        return Stream.of(
                -0.1,
                3.1,
                Double.NaN,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);
    }
}
