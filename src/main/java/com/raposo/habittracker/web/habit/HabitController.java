package com.raposo.habittracker.web.habit;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.raposo.habittracker.application.CreateHabitUseCase;
import com.raposo.habittracker.application.ListHabitsUseCase;
import com.raposo.habittracker.application.habit.CreateHabitInput;
import com.raposo.habittracker.domain.Habit;

@RestController
@RequestMapping("/api/habits")
class HabitController {

    private final ListHabitsUseCase listHabitsUseCase;
    private final CreateHabitUseCase createHabitUseCase;
    private final HabitResponseMapper mapper;

    HabitController(
            ListHabitsUseCase listHabitsUseCase,
            CreateHabitUseCase createHabitUseCase,
            HabitResponseMapper mapper) {
        this.listHabitsUseCase = listHabitsUseCase;
        this.createHabitUseCase = createHabitUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    HabitCatalogResponse list() {
        List<Habit> habits = listHabitsUseCase.execute();

        return mapper.toCatalogResponse(habits);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    HabitResponse create(@RequestBody CreateHabitRequest request) {
        Habit habit = createHabitUseCase.execute(new CreateHabitInput(
                request.habitName(),
                request.cadence()));

        return mapper.toResponse(habit);
    }
}
