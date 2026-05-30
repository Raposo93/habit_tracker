package com.raposo.habittracker;

import java.util.List;

public class SheetReader implements HabitEntryReader {
    private final GoogleAuth auth;
    private final String spreadsheetId;

    public SheetReader(GoogleAuth auth, String spreadsheetId) {
        this.auth = auth;
        this.spreadsheetId = spreadsheetId;
    }

    @Override
    public List<HabitEntry> readEntries() {
        return List.of();
    }
}
