package com.raposo.habittracker;

import java.nio.file.Path;

public class Config {
    public final String APPLICATION_NAME = "Habit Tracker";
    public final Path DB_PATH = Path.of("db", "habit_tracker.db");
    public final Path CREDENTIALS_PATH = Path.of("credentials.json");
    public final Path TOKENS_DIRECTORY_PATH = Path.of("tokens");
    public final String SPREADSHEET_ID = "";
}