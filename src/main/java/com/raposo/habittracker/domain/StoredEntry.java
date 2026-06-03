package com.raposo.habittracker.domain;

public record StoredEntry(
                double score,
                String note) {
        public StoredEntry {
                note = note == null ? "" : note;
        }
}
