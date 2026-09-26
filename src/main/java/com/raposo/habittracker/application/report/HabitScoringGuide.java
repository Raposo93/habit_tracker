package com.raposo.habittracker.application.report;

import com.raposo.habittracker.domain.HabitId;

public record HabitScoringGuide(HabitId habitId, String habitName, String scoringGuide) {
}
