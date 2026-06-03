package com.raposo.habittracker.application.port;

import java.util.List;

import com.raposo.habittracker.domain.HabitEntry;

public interface HabitEntryReader {
    List<HabitEntry> readEntries();

}
