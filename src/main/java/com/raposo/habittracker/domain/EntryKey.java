package com.raposo.habittracker.domain;

import java.time.LocalDate;

public record EntryKey(
                LocalDate entryDate,
                String habit) {
}
