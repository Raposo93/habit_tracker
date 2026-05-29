package com.raposo.habittracker;

import java.util.List;

public class SheetReader {
    private final GoogleAuth auth;
    private final String spreadsheetId;

    public SheetReader(GoogleAuth auth, String spreadsheetId) {
        this.auth = auth;
        this.spreadsheetId = spreadsheetId;
    }

    public List<HabitEntry> readWeekEntries() {
        return List.of();
    }
}