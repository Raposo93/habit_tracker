package com.raposo.habittracker.application.habit;

public class HabitNameAlreadyExistsException extends RuntimeException {

    public HabitNameAlreadyExistsException(String name) {
        super("Habit name already exists: " + name);
    }
}
