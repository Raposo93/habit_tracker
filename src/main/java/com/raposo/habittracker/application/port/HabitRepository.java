package com.raposo.habittracker.application.port;

import java.util.List;
import java.util.Optional;

import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitId;

public interface HabitRepository {
    boolean create(Habit habit);

    Optional<Habit> findById(HabitId id);

    Optional<Habit> findByExactName(String name);

    List<Habit> findActive();

    List<Habit> findAll();
}
