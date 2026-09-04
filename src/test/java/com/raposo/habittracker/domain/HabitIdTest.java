package com.raposo.habittracker.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class HabitIdTest {

    @Test
    void whenGenerateThenValueIsAValidUuid() {
        HabitId habitId = HabitId.generate();

        assertDoesNotThrow(() -> UUID.fromString(habitId.value()));
    }
}
