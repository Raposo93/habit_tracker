package com.raposo.habittracker.application.entry;

import java.util.Optional;

import com.raposo.habittracker.domain.HabitId;
import com.raposo.habittracker.domain.StoredEntry;

public record HabitEntryContext(
        HabitId habitId,
        String habitName,
        Optional<StoredEntry> entry) {
}
