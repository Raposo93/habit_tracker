package com.raposo.habittracker.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.config.Config;
import com.raposo.habittracker.infrastructure.sqlite.SqliteHabitEntryRepository;
import com.raposo.habittracker.infrastructure.sqlite.SqliteHabitRepository;

@Configuration
public class PersistenceConfiguration {

    @Bean
    Config config() {
        return new Config();
    }

    @Bean
    HabitEntryRepository habitEntryRepository(Config config) {
        return new SqliteHabitEntryRepository(config.dbPath());
    }

    @Bean
    HabitRepository habitRepository(Config config) {
        return new SqliteHabitRepository(config.dbPath());
    }
}
