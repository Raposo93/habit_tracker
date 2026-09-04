package com.raposo.habittracker.web.habit;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raposo.habittracker.application.ListHabitsUseCase;
import com.raposo.habittracker.domain.Habit;

@RestController
@RequestMapping("/api/habits")
class HabitController {

    private final ListHabitsUseCase listHabitsUseCase;
    private final HabitCatalogResponseMapper mapper;

    HabitController(
            ListHabitsUseCase listHabitsUseCase,
            HabitCatalogResponseMapper mapper) {
        this.listHabitsUseCase = listHabitsUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    HabitCatalogResponse list() {
        List<Habit> habits = listHabitsUseCase.execute();

        return mapper.toResponse(habits);
    }
}
