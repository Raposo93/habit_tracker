package com.raposo.habittracker.web.entry;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.raposo.habittracker.application.entry.HabitEntryAlreadyExistsException;
import com.raposo.habittracker.application.entry.HabitEntryNotFoundException;
import com.raposo.habittracker.application.entry.InvalidHabitEntryScoreException;
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

    @ExceptionHandler(InvalidHabitEntryScoreException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    DailyEntryErrorResponse handleInvalidScore(InvalidHabitEntryScoreException exception) {
        return new DailyEntryErrorResponse("INVALID_SCORE", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    DailyEntryErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        if (LocalDate.class.equals(exception.getRequiredType())) {
            return invalidDate();
        }

        return new DailyEntryErrorResponse("INVALID_ENTRY", "Entry request contains an invalid value");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    DailyEntryErrorResponse handleMissingParameter(MissingServletRequestParameterException exception) {
        if ("date".equals(exception.getParameterName())) {
            return invalidDate();
        }

        return new DailyEntryErrorResponse("INVALID_ENTRY", "Entry request is missing a required value");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    DailyEntryErrorResponse handleInvalidEntry(IllegalArgumentException exception) {
        return new DailyEntryErrorResponse("INVALID_ENTRY", exception.getMessage());
    }

    private DailyEntryErrorResponse invalidDate() {
        return new DailyEntryErrorResponse("INVALID_DATE", "Date must use YYYY-MM-DD format");
    }
}
