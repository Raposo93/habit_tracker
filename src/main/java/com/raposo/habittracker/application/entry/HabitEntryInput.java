package com.raposo.habittracker.application.entry;

import java.time.LocalDate;

import com.raposo.habittracker.domain.HabitId;

public record HabitEntryInput(
        LocalDate date,
        HabitId habitId,
        double score,
        String note) {

    public HabitEntryInput {
        if (date == null) {
            throw new IllegalArgumentException("Entry date cannot be null");
        }
        if (habitId == null) {
            throw new IllegalArgumentException("HabitId cannot be null");
        }
        if (!Double.isFinite(score) || score < 0 || score > 3) {
            throw new IllegalArgumentException("Score must be between 0 and 3");
        }

        note = note == null ? "" : note;
    }
}
