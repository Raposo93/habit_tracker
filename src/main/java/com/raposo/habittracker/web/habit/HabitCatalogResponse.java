package com.raposo.habittracker.web.habit;

import java.util.List;

public record HabitCatalogResponse(
        List<HabitResponse> habits) {
}
