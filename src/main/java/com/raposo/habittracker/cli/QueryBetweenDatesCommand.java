package com.raposo.habittracker.cli;

import com.raposo.habittracker.application.GetEntriesBetweenDatesUseCase;
import com.raposo.habittracker.domain.DateRange;

public class QueryBetweenDatesCommand implements Command {

    public QueryBetweenDatesCommand(GetEntriesBetweenDatesUseCase getEntries, DateRange range) {
        // TODO
    }

    @Override
    public void execute() {
        // TODO
        System.out.println("Query between dates is not implemented yet.");
    }

}
