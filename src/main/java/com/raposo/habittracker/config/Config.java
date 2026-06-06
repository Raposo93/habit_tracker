package com.raposo.habittracker.config;

import java.nio.file.Path;

public class Config {
    private static final String DEFAULT_APPLICATION_NAME = "Habit Tracker";
    private static final Path DEFAULT_DB_PATH = Path.of("db", "habit_tracker.db");
    private static final Path DEFAULT_CREDENTIALS_PATH = Path.of("credentials.json");
    private static final Path DEFAULT_TOKENS_DIRECTORY_PATH = Path.of("tokens");

    private final String applicationName;
    private final Path dbPath;
    private final Path credentialsPath;
    private final Path tokensDirectoryPath;
    private final String spreadsheetId;

    public Config() {
        this.applicationName = DEFAULT_APPLICATION_NAME;
        this.dbPath = pathFromEnvOrDefault("DB_PATH", DEFAULT_DB_PATH);
        this.credentialsPath = pathFromEnvOrDefault("CREDENTIALS_PATH", DEFAULT_CREDENTIALS_PATH);
        this.tokensDirectoryPath = pathFromEnvOrDefault("TOKENS_DIRECTORY_PATH", DEFAULT_TOKENS_DIRECTORY_PATH);
        this.spreadsheetId = requiredEnv("SPREADSHEET_ID");
    }

    public String applicationName() {
        return applicationName;
    }

    public Path dbPath() {
        return dbPath;
    }

    public Path credentialsPath() {
        return credentialsPath;
    }

    public Path tokensDirectoryPath() {
        return tokensDirectoryPath;
    }

    public String spreadsheetId() {
        return spreadsheetId;
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is not configured.");
        }

        return value;
    }

    private static Path pathFromEnvOrDefault(String name, Path defaultValue) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return Path.of(value);
    }
}