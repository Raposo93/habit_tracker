package com.raposo.habittracker.web.habit;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.raposo.habittracker.application.habit.HabitNameAlreadyExistsException;
import com.raposo.habittracker.application.habit.InvalidHabitCadenceException;
import com.raposo.habittracker.application.habit.InvalidHabitNameException;

@RestControllerAdvice(assignableTypes = HabitController.class)
class HabitExceptionHandler {

    @ExceptionHandler(HabitNameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    HabitErrorResponse handleNameAlreadyExists(HabitNameAlreadyExistsException exception) {
        return new HabitErrorResponse("HABIT_NAME_ALREADY_EXISTS", exception.getMessage());
    }

    @ExceptionHandler(InvalidHabitNameException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    HabitErrorResponse handleInvalidName(InvalidHabitNameException exception) {
        return new HabitErrorResponse("INVALID_HABIT_NAME", exception.getMessage());
    }

    @ExceptionHandler(InvalidHabitCadenceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    HabitErrorResponse handleInvalidCadence(InvalidHabitCadenceException exception) {
        return new HabitErrorResponse("INVALID_HABIT_CADENCE", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    HabitErrorResponse handleUnreadableRequest(HttpMessageNotReadableException exception) {
        return new HabitErrorResponse("INVALID_HABIT", "Habit request must contain valid JSON");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    HabitErrorResponse handleInvalidHabit(IllegalArgumentException exception) {
        return new HabitErrorResponse("INVALID_HABIT", exception.getMessage());
    }
}
