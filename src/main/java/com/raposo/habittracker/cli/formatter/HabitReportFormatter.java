package com.raposo.habittracker.cli.formatter;

import com.raposo.habittracker.application.report.HabitReport;

public interface HabitReportFormatter {
    String format(HabitReport report);
}
