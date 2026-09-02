package com.raposo.habittracker.web.entry;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raposo.habittracker.application.GetDailyEntryContextUseCase;
import com.raposo.habittracker.application.entry.DailyEntryContext;

@RestController
@RequestMapping("/api/entries")
class DailyEntryController {

    private final GetDailyEntryContextUseCase getDailyEntryContextUseCase;
    private final DailyEntryResponseMapper mapper;

    DailyEntryController(
            GetDailyEntryContextUseCase getDailyEntryContextUseCase,
            DailyEntryResponseMapper mapper) {
        this.getDailyEntryContextUseCase = getDailyEntryContextUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/context")
    DailyEntryResponse context(@RequestParam LocalDate date) {
        DailyEntryContext context = getDailyEntryContextUseCase.execute(date);

        return mapper.toResponse(context);
    }
}
