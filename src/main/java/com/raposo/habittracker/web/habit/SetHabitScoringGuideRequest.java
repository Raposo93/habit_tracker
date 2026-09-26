package com.raposo.habittracker.web.habit;

import com.fasterxml.jackson.annotation.JsonProperty;

record SetHabitScoringGuideRequest(@JsonProperty(value = "scoringGuide", required = true) String scoringGuide) {
}
