package com.raposo.habittracker.application.port;

import java.util.List;
import java.util.Optional;

import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitId;

public interface HabitRepository {
    boolean create(Habit habit);

    /** Sets the desired state; returns false only when the habit does not exist. */
    boolean setActive(HabitId habitId, boolean active);

    enum RenameResult {
        RENAMED, NOT_FOUND, NAME_ALREADY_EXISTS
    }

    RenameResult rename(HabitId habitId, String name);

    /** Reorders the complete catalog atomically; false means the catalog does not match. */
    boolean reorder(List<HabitId> habitIds);

    Optional<Habit> findById(HabitId id);

    List<Habit> findActive();

    List<Habit> findAll();
}
