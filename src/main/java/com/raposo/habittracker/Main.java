package com.raposo.habittracker;

public class Main {
    public static void main(String[] args) {
        Config config = new Config();

        HabitEntryRepository repository = new SqliteHabitEntryRepository(config.DB_PATH);

        Command command = CommandParser.parse(
                args,
                config,
                repository);

        command.execute();
    }
}