package com.raposo.habittracker.cli;

public class HelpCommand implements Command {
    @Override
    public void execute() {
        System.out.println("Usage:");
        System.out.println("  --import");
        System.out.println("  --query-btw-dates <start-date> <end-date>");
        System.out.println("  --query-last-week");
    }

}
