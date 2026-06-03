package com.raposo.habittracker.cli;

import com.raposo.habittracker.application.ImportEntriesUseCase;

public class ImportCommand implements Command {
    private final ImportEntriesUseCase importEntries;

    public ImportCommand(ImportEntriesUseCase importEntries) {
        this.importEntries = importEntries;
    }

    @Override
    public void execute() {
        importEntries.execute();
    }
}
