package com.raposo.habittracker.config;

import java.nio.file.Path;

public class Config {
    private static final Path DEFAULT_DB_PATH = Path.of("db", "habit_tracker.db");

    private final Path dbPath;

    public Config() {
        this.dbPath = pathFromEnvOrDefault("DB_PATH", DEFAULT_DB_PATH);
    }

    public Path dbPath() {
        return dbPath;
    }

    private static Path pathFromEnvOrDefault(String name, Path defaultValue) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return Path.of(value);
    }
}
