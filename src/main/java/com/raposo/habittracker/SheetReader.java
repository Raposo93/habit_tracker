package com.raposo.habittracker;

import com.google.api.services.sheets.v4.Sheets;

import java.util.List;

public class SheetReader implements HabitEntryReader {
    private final Sheets sheetsService;
    private final String spreadsheetId;

    public SheetReader(Sheets sheetsService, String spreadsheetId) {
        this.sheetsService = sheetsService;
        this.spreadsheetId = spreadsheetId;
    }

    @Override
    public List<HabitEntry> readEntries() {
        return List.of();
    }
}
