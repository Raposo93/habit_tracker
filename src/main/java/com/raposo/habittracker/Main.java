package com.raposo.habittracker;

import com.raposo.habittracker.application.port.HabitEntryRepository;
import com.raposo.habittracker.application.port.HabitRepository;
import com.raposo.habittracker.cli.Command;
import com.raposo.habittracker.cli.CommandParser;
import com.raposo.habittracker.config.Config;
import com.raposo.habittracker.infrastructure.sqlite.SqliteHabitEntryRepository;
import com.raposo.habittracker.infrastructure.sqlite.SqliteHabitRepository;

public class Main {
    public static void main(String[] args) {
        Config config = new Config();

        HabitRepository habitRepository = new SqliteHabitRepository(config.dbPath());
        HabitEntryRepository entryRepository = new SqliteHabitEntryRepository(config.dbPath());

        Command command = CommandParser.parse(
                args,
                config,
                entryRepository,
                habitRepository);

        command.execute();
    }
}
