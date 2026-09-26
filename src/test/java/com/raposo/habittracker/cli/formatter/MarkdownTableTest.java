package com.raposo.habittracker.cli.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class MarkdownTableTest {
    @Test
    void rendersCompactCellsRegardlessOfTheirLengths() {
        assertEquals("""
                | habit | score | note |
                | --- | --- | --- |
                | Sleep | 1.50 | A long note that must not pad other rows |
                | Exercise | 0.00 |  |
                """, MarkdownTable.render(List.of("habit", "score", "note"), List.of(
                        List.of("Sleep", "1.50", "A long note that must not pad other rows"),
                        List.of("Exercise", "0.00", ""))));
    }

    @Test
    void preservesExistingEscapingAndWhitespaceNormalization() {
        assertEquals("| habit\\|name | note |\n| --- | --- |\n| Sleep\\|Rest | First  Second |\n|  |  |\n",
                MarkdownTable.render(List.of(" habit|name ", "note"), List.of(
                        List.of(" Sleep|Rest ", " First\r\nSecond "),
                        Arrays.asList(null, null))));
    }
}
