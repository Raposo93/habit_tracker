package com.raposo.habittracker.application.report;

public record ReportContext(
        String scoreScale,
        String weekDefinition,
        String missingScoreMeaning
) {
}
