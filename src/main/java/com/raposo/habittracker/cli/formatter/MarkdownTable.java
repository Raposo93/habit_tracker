package com.raposo.habittracker.cli.formatter;

import java.util.List;

final class MarkdownTable {

    private MarkdownTable() {
    }

    static String render(List<String> headers, List<List<String>> rows) {
        StringBuilder output = new StringBuilder();
        int columnCount = headers.size();

        appendRow(output, headers, columnCount);
        output.append("|").append(" --- |".repeat(columnCount)).append("\n");

        for (List<String> row : rows) {
            appendRow(output, row, columnCount);
        }

        return output.toString();
    }

    private static void appendRow(StringBuilder output, List<String> values, int columnCount) {
        output.append("|");

        for (int index = 0; index < columnCount; index++) {
            String value = index < values.size() ? values.get(index) : "";
            output.append(" ").append(safeCell(value)).append(" |");
        }

        output.append("\n");
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
