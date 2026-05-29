package com.raposo.habittracker;

import java.time.LocalDate;

public record HabitEntry(
        LocalDate entryDate,
        String habit,
        double score,
        String note
) {
}