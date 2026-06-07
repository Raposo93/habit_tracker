package com.raposo.habittracker.cli.formatter;

import java.util.ArrayList;
import java.util.List;

final class MarkdownTable {

    private MarkdownTable() {
    }

    static String render(List<String> headers, List<List<String>> rows) {
        List<List<String>> safeRows = rows.stream()
                .map(row -> row.stream()
                        .map(MarkdownTable::safeCell)
                        .toList())
                .toList();

        List<String> safeHeaders = headers.stream()
                .map(MarkdownTable::safeCell)
                .toList();

        List<Integer> widths = columnWidths(safeHeaders, safeRows);

        StringBuilder output = new StringBuilder();

        appendRow(output, safeHeaders, widths);
        appendSeparator(output, widths);

        for (List<String> row : safeRows) {
            appendRow(output, row, widths);
        }

        return output.toString();
    }

    private static List<Integer> columnWidths(
            List<String> headers,
            List<List<String>> rows
    ) {
        List<Integer> widths = new ArrayList<>();

        for (String header : headers) {
            widths.add(header.length());
        }

        for (List<String> row : rows) {
            for (int index = 0; index < headers.size(); index++) {
                String value = index < row.size() ? row.get(index) : "";
                widths.set(index, Math.max(widths.get(index), value.length()));
            }
        }

        return widths;
    }

    private static void appendRow(
            StringBuilder output,
            List<String> values,
            List<Integer> widths
    ) {
        output.append("|");

        for (int index = 0; index < widths.size(); index++) {
            String value = index < values.size() ? values.get(index) : "";

            output.append(" ")
                    .append(padRight(value, widths.get(index)))
                    .append(" |");
        }

        output.append("\n");
    }

    private static void appendSeparator(
            StringBuilder output,
            List<Integer> widths
    ) {
        output.append("|");

        for (int width : widths) {
            output.append("-".repeat(width + 2))
                    .append("|");
        }

        output.append("\n");
    }

    private static String padRight(String value, int width) {
        return value + " ".repeat(width - value.length());
    }

    private static String safeCell(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("|", "\\|")
                .trim();
    }
}