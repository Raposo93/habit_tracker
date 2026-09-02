package com.raposo.habittracker.application;

import java.util.Objects;

import com.raposo.habittracker.application.entry.HabitEntryAlreadyExistsException;
import com.raposo.habittracker.application.entry.HabitEntryInput;
import com.raposo.habittracker.application.entry.UnknownHabitException;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.domain.StoredEntry;

public class CreateHabitEntryUseCase {

    private final HabitRepository habitRepository;
    private final HabitEntryRepository entryRepository;

    public CreateHabitEntryUseCase(
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

        boolean created = entryRepository.createEntry(
                input.date(),
                input.habitId(),
                new StoredEntry(input.score(), input.note()));

        if (!created) {
            throw new HabitEntryAlreadyExistsException(input.date(), input.habitId());
        }
    }
}
