package com.raposo.habittracker;

import java.nio.file.Path;

public class Config {
    public final String APPLICATION_NAME = "Habit Tracker";
    public final Path DB_PATH = Path.of("db", "habit_tracker.db");
    public final Path CREDENTIALS_PATH = Path.of("credentials.json");
    public final Path TOKENS_DIRECTORY_PATH = Path.of("tokens");
    public final String SPREADSHEET_ID = System.getenv("SPREADSHEET_ID");

    public Config() {
        if (SPREADSHEET_ID == null || SPREADSHEET_ID.isBlank()) {
            throw new IllegalStateException(
                    "SPREADSHEET_ID is not configured. "
                            + "\n Run: export SPREADSHEET_ID=\"your-spreadsheet-id\"");
        }
    }
}
