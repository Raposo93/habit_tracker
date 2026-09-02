package com.raposo.habittracker.web.entry;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.raposo.habittracker.application.entry.HabitEntryAlreadyExistsException;
import com.raposo.habittracker.application.entry.HabitEntryNotFoundException;
import com.raposo.habittracker.application.entry.UnknownHabitException;

@RestControllerAdvice(assignableTypes = DailyEntryController.class)
class DailyEntryExceptionHandler {

    @ExceptionHandler(HabitEntryAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    DailyEntryErrorResponse handleAlreadyExists(HabitEntryAlreadyExistsException exception) {
        return new DailyEntryErrorResponse("ENTRY_ALREADY_EXISTS", exception.getMessage());
    }

    @ExceptionHandler(HabitEntryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    DailyEntryErrorResponse handleEntryNotFound(HabitEntryNotFoundException exception) {
        return new DailyEntryErrorResponse("ENTRY_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(UnknownHabitException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    DailyEntryErrorResponse handleUnknownHabit(UnknownHabitException exception) {
        return new DailyEntryErrorResponse("UNKNOWN_HABIT", exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    DailyEntryErrorResponse handleInvalidEntry(IllegalArgumentException exception) {
        return new DailyEntryErrorResponse("INVALID_ENTRY", exception.getMessage());
    }
}
