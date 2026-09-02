package com.raposo.habittracker.application;

import java.util.Objects;

import com.raposo.habittracker.application.entry.HabitEntryInput;
import com.raposo.habittracker.application.entry.HabitEntryNotFoundException;
import com.raposo.habittracker.application.entry.UnknownHabitException;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.StoredEntry;

public class UpdateHabitEntryUseCase {

    private final HabitRepository habitRepository;
    private final HabitEntryRepository entryRepository;

    public UpdateHabitEntryUseCase(
            HabitRepository habitRepository,
            HabitEntryRepository entryRepository) {
        this.habitRepository = Objects.requireNonNull(habitRepository);
        this.entryRepository = Objects.requireNonNull(entryRepository);
    }

    public void execute(HabitEntryInput input) {
        Objects.requireNonNull(input);

        if (habitRepository.findById(input.habitId()).isEmpty()) {
            throw new UnknownHabitException(input.habitId());
        }

        boolean updated = entryRepository.updateEntry(
                input.date(),
                input.habitId(),
                new StoredEntry(input.score(), input.note()));

        if (!updated) {
            throw new HabitEntryNotFoundException(input.date(), input.habitId());
        }
    }
}
