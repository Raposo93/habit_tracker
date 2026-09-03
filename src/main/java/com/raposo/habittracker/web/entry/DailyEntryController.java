package com.raposo.habittracker.web.entry;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.raposo.habittracker.application.CreateHabitEntryUseCase;
import com.raposo.habittracker.application.GetDailyEntryContextUseCase;
import com.raposo.habittracker.application.UpdateHabitEntryUseCase;
import com.raposo.habittracker.application.entry.DailyEntryContext;
import com.raposo.habittracker.application.entry.HabitEntryInput;
import com.raposo.habittracker.application.entry.InvalidHabitEntryScoreException;
import com.raposo.habittracker.domain.HabitId;

@RestController
@RequestMapping("/api/entries")
class DailyEntryController {

    private final GetDailyEntryContextUseCase getDailyEntryContextUseCase;
    private final CreateHabitEntryUseCase createHabitEntryUseCase;
    private final UpdateHabitEntryUseCase updateHabitEntryUseCase;
    private final DailyEntryResponseMapper mapper;

    DailyEntryController(
            GetDailyEntryContextUseCase getDailyEntryContextUseCase,
            CreateHabitEntryUseCase createHabitEntryUseCase,
            UpdateHabitEntryUseCase updateHabitEntryUseCase,
            DailyEntryResponseMapper mapper) {
        this.getDailyEntryContextUseCase = getDailyEntryContextUseCase;
        this.createHabitEntryUseCase = createHabitEntryUseCase;
        this.updateHabitEntryUseCase = updateHabitEntryUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/context")
    DailyEntryResponse context(@RequestParam LocalDate date) {
        DailyEntryContext context = getDailyEntryContextUseCase.execute(date);

        return mapper.toResponse(context);
    }

    @PostMapping("/{date}/{habitId}")
    @ResponseStatus(HttpStatus.CREATED)
    void create(
            @PathVariable LocalDate date,
            @PathVariable String habitId,
            @RequestBody HabitEntryRequest request) {
        createHabitEntryUseCase.execute(toInput(date, habitId, request));
    }

    @PutMapping("/{date}/{habitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void update(
            @PathVariable LocalDate date,
            @PathVariable String habitId,
            @RequestBody HabitEntryRequest request) {
        updateHabitEntryUseCase.execute(toInput(date, habitId, request));
    }

    private HabitEntryInput toInput(
            LocalDate date,
            String habitId,
            HabitEntryRequest request) {
        if (request.score() == null) {
            throw InvalidHabitEntryScoreException.missing();
        }

        return new HabitEntryInput(
                date,
                HabitId.of(habitId),
                request.score(),
                request.note());
    }
}
