package com.raposo.habittracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HabitTrackerApplication {

    public static void main(String[] args) {
        if (args.length > 0 && isCliCommand(args[0])) {
            Main.main(args);
            return;
        }

        SpringApplication.run(HabitTrackerApplication.class, args);
    }

    private static boolean isCliCommand(String argument) {
        return switch (argument) {
            case "--query-last-week", "--query-between-dates", "--help" -> true;
            default -> false;
        };
    }
}
