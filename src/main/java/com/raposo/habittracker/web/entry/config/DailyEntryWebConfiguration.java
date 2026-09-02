package com.raposo.habittracker.web.entry.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.raposo.habittracker.application.GetDailyEntryContextUseCase;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.web.entry.DailyEntryResponseMapper;

@Configuration
public class DailyEntryWebConfiguration {

    @Bean
    GetDailyEntryContextUseCase getDailyEntryContextUseCase(
            HabitRepository habitRepository,
            HabitEntryRepository habitEntryRepository) {
        return new GetDailyEntryContextUseCase(habitRepository, habitEntryRepository);
    }

    @Bean
    DailyEntryResponseMapper dailyEntryResponseMapper() {
        return new DailyEntryResponseMapper();
    }
}
