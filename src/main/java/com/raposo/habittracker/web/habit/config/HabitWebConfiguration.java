package com.raposo.habittracker.web.habit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.raposo.habittracker.application.CreateHabitUseCase;
import com.raposo.habittracker.application.ListHabitsUseCase;
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
    HabitResponseMapper habitResponseMapper() {
        return new HabitResponseMapper();
    }
}
