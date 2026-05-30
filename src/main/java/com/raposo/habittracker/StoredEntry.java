package com.raposo.habittracker;

public record StoredEntry(
        double score,
        String note
) {
        public StoredEntry {
                note = note == null ? "" : note;
        }
}
