package com.raposo.habittracker.web.habit;

record SetHabitActiveRequest(Boolean active) {
    SetHabitActiveRequest {
        if (active == null) {
            throw new IllegalArgumentException("Habit active state is required");
        }
    }
}
