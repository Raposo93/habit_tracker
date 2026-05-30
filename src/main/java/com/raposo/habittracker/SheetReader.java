package com.raposo.habittracker;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SheetReader implements HabitEntryReader {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Sheets sheetsService;
    private final String spreadsheetId;

    public SheetReader(Sheets sheetsService, String spreadsheetId) {
        this.sheetsService = sheetsService;
        this.spreadsheetId = spreadsheetId;
    }

    @Override
    public List<HabitEntry> readEntries() {
        try {
            List<String> habitNames = readHabitNames();

            if (habitNames.isEmpty()) {
                return List.of();
            }

            List<String> dates = readWeekDates();
            List<SheetCell> weeklyData = readWeeklyData(habitNames.size());

            return buildEntries(habitNames, dates, weeklyData);

        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read entries from Google Sheets", exception);
        }
    }

    private List<String> readHabitNames() throws IOException {
        ValueRange response = sheetsService.spreadsheets()
                .values()
                .get(spreadsheetId, "C1:1")
                .execute();

        List<List<Object>> rows = response.getValues();

        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        return rows.getFirst().stream()
                .map(value -> value.toString().trim())
                .filter(value -> !value.isBlank())
                .toList();
    }

    private List<String> readWeekDates() throws IOException {
        ValueRange response = sheetsService.spreadsheets()
                .values()
                .get(spreadsheetId, "B2:B8")
                .execute();

        List<List<Object>> rows = response.getValues();

        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        List<String> dates = new ArrayList<>();

        for (List<Object> row : rows) {
            if (row.isEmpty()) {
                dates.add("");
            } else {
                dates.add(row.getFirst().toString().trim());
            }
        }

        return dates;
    }

    private List<SheetCell> readWeeklyData(int habitCount) throws IOException {
        String endColumn = columnName(3 + habitCount - 1);
        String rangeToRead = "C2:" + endColumn + "8";

        Spreadsheet response = sheetsService.spreadsheets()
                .get(spreadsheetId)
                .setRanges(List.of(rangeToRead))
                .setIncludeGridData(true)
                .execute();

        List<SheetCell> weeklyData = new ArrayList<>();

        List<Sheet> sheets = response.getSheets();

        if (sheets == null || sheets.isEmpty()) {
            return weeklyData;
        }

        List<GridData> data = sheets.getFirst().getData();

        if (data == null || data.isEmpty()) {
            return weeklyData;
        }

        List<RowData> rows = data.getFirst().getRowData();

        if (rows == null) {
            return weeklyData;
        }

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            RowData row = rows.get(rowIndex);
            List<CellData> cells = row.getValues();

            if (cells == null) {
                continue;
            }

            for (int columnIndex = 0; columnIndex < cells.size(); columnIndex++) {
                CellData cell = cells.get(columnIndex);

                String value = cell.getFormattedValue() == null
                        ? ""
                        : cell.getFormattedValue();

                String note = cell.getNote() == null
                        ? ""
                        : cell.getNote();

                weeklyData.add(new SheetCell(
                        rowIndex,
                        columnIndex,
                        value,
                        note));
            }
        }

        return weeklyData;
    }

    private List<HabitEntry> buildEntries(
            List<String> habitNames,
            List<String> dates,
            List<SheetCell> weeklyData) {
        List<HabitEntry> entries = new ArrayList<>();

        for (SheetCell cell : weeklyData) {
            if (cell.rowIndex() >= dates.size()
                    || cell.columnIndex() >= habitNames.size()) {
                continue;
            }

            String rawScore = cell.value().trim();

            if (rawScore.isBlank() || rawScore.equalsIgnoreCase("none")) {
                continue;
            }

            entries.add(new HabitEntry(
                    parseDate(dates.get(cell.rowIndex())),
                    habitNames.get(cell.columnIndex()),
                    parseScore(rawScore),
                    cell.note().trim()));
        }

        return entries;
    }

    private static LocalDate parseDate(String value) {
        return LocalDate.parse(value.trim(), DATE_FORMATTER);
    }

    private static double parseScore(String value) {
        return Double.parseDouble(value.trim().replace(",", "."));
    }

    private static String columnName(int columnNumber) {
        StringBuilder columnName = new StringBuilder();

        while (columnNumber > 0) {
            columnNumber--;
            columnName.insert(0, (char) ('A' + columnNumber % 26));
            columnNumber /= 26;
        }

        return columnName.toString();
    }

    private record SheetCell(
            int rowIndex,
            int columnIndex,
            String value,
            String note) {
    }
}