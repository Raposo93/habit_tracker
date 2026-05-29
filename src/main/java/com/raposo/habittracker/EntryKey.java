package com.raposo.habittracker;

import java.time.LocalDate;

public record EntryKey(
        LocalDate entryDate,
        String habit
) {
}