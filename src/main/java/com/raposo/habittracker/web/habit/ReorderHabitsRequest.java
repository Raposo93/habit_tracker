package com.raposo.habittracker.web.habit;

import java.util.List;

record ReorderHabitsRequest(List<String> habitIds) {
}
