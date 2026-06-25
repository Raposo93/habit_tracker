package com.raposo.habittracker.web.report.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.raposo.habittracker.application.GetHabitReportBetweenDatesUseCase;
import com.raposo.habittracker.application.GetHabitReportLastWeekUseCase;
import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.config.Config;
import com.raposo.habittracker.infrastructure.sqlite.SqliteHabitEntryRepository;
import com.raposo.habittracker.web.report.ReportResponseMapper;

@Configuration
public class ReportWebConfiguration {

    @Bean
    Config config() {
        return new Config();
    }

    @Bean
    HabitEntryRepository habitEntryRepository(Config config) {
        return new SqliteHabitEntryRepository(config.dbPath());
    }

    @Bean
    GetHabitReportBetweenDatesUseCase getHabitReportBetweenDatesUseCase(
            HabitEntryRepository habitEntryRepository) {
        return new GetHabitReportBetweenDatesUseCase(habitEntryRepository);
    }

    @Bean
    GetHabitReportLastWeekUseCase getHabitReportLastWeekUseCase(
            GetHabitReportBetweenDatesUseCase getHabitReportBetweenDatesUseCase) {
        return new GetHabitReportLastWeekUseCase(getHabitReportBetweenDatesUseCase);
    }

    @Bean
    ReportResponseMapper reportResponseMapper() {
        return new ReportResponseMapper();
    }

}
