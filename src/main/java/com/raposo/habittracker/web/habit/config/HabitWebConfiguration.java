package com.raposo.habittracker.web.habit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.raposo.habittracker.application.CreateHabitUseCase;
import com.raposo.habittracker.application.ListHabitsUseCase;
import com.raposo.habittracker.application.RenameHabitUseCase;
import com.raposo.habittracker.application.ReorderHabitsUseCase;
import com.raposo.habittracker.application.SetHabitActiveUseCase;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.web.habit.HabitResponseMapper;

@Configuration
public class HabitWebConfiguration {

    @Bean
    ListHabitsUseCase listHabitsUseCase(HabitRepository habitRepository) {
        return new ListHabitsUseCase(habitRepository);
    }

    @Bean
    CreateHabitUseCase createHabitUseCase(HabitRepository habitRepository) {
        return new CreateHabitUseCase(habitRepository);
    }

    @Bean
    SetHabitActiveUseCase setHabitActiveUseCase(HabitRepository habitRepository) {
        return new SetHabitActiveUseCase(habitRepository);
    }

    @Bean
    RenameHabitUseCase renameHabitUseCase(HabitRepository habitRepository) {
        return new RenameHabitUseCase(habitRepository);
    }

    @Bean
    ReorderHabitsUseCase reorderHabitsUseCase(HabitRepository habitRepository) {
        return new ReorderHabitsUseCase(habitRepository);
    }

    @Bean
    HabitResponseMapper habitResponseMapper() {
        return new HabitResponseMapper();
    }
}
