package com.raposo.habittracker.domain;

import java.time.LocalDate;

public record HabitEntry(
                LocalDate entryDate,
                String habit,
                double score,
                String note) {

        public HabitEntry {
                note = note == null ? "" : note;
        }
}
