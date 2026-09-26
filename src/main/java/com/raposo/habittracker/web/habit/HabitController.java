package com.raposo.habittracker.web.habit;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.raposo.habittracker.application.CreateHabitUseCase;
import com.raposo.habittracker.application.ListHabitsUseCase;
import com.raposo.habittracker.application.RenameHabitUseCase;
import com.raposo.habittracker.application.ReorderHabitsUseCase;
import com.raposo.habittracker.application.SetHabitActiveUseCase;
import com.raposo.habittracker.application.SetHabitScoringGuideUseCase;
import com.raposo.habittracker.application.habit.CreateHabitInput;
import com.raposo.habittracker.domain.Habit;
import com.raposo.habittracker.domain.HabitId;

@RestController
@RequestMapping("/api/habits")
class HabitController {

    private final ListHabitsUseCase listHabitsUseCase;
    private final CreateHabitUseCase createHabitUseCase;
    private final HabitResponseMapper mapper;
    private final SetHabitActiveUseCase setHabitActiveUseCase;
    private final RenameHabitUseCase renameHabitUseCase;
    private final ReorderHabitsUseCase reorderHabitsUseCase;
    private final SetHabitScoringGuideUseCase setHabitScoringGuideUseCase;

    HabitController(
            ListHabitsUseCase listHabitsUseCase,
            CreateHabitUseCase createHabitUseCase,
            HabitResponseMapper mapper,
            SetHabitActiveUseCase setHabitActiveUseCase,
            RenameHabitUseCase renameHabitUseCase,
            ReorderHabitsUseCase reorderHabitsUseCase,
            SetHabitScoringGuideUseCase setHabitScoringGuideUseCase) {
        this.listHabitsUseCase = listHabitsUseCase;
        this.createHabitUseCase = createHabitUseCase;
        this.mapper = mapper;
        this.setHabitActiveUseCase = setHabitActiveUseCase;
        this.renameHabitUseCase = renameHabitUseCase;
        this.reorderHabitsUseCase = reorderHabitsUseCase;
        this.setHabitScoringGuideUseCase = setHabitScoringGuideUseCase;
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
                request.cadence(),
                request.scoringGuide()));

        return mapper.toResponse(habit);
    }

    @PutMapping("/{habitId}/active")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void setActive(@PathVariable String habitId, @RequestBody SetHabitActiveRequest request) {
        setHabitActiveUseCase.execute(HabitId.of(habitId), request.active());
    }

    @PutMapping("/{habitId}/name")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void rename(@PathVariable String habitId, @RequestBody RenameHabitRequest request) {
        renameHabitUseCase.execute(HabitId.of(habitId), request.habitName());
    }

    @PutMapping("/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reorder(@RequestBody ReorderHabitsRequest request) {
        reorderHabitsUseCase.execute(request.habitIds());
    }

    @PutMapping("/{habitId}/scoring-guide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void setScoringGuide(@PathVariable String habitId, @RequestBody SetHabitScoringGuideRequest request) {
        setHabitScoringGuideUseCase.execute(HabitId.of(habitId), request.scoringGuide());
    }
}
