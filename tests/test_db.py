import sqlite3
from datetime import date

from habit_tracker.db import import_entries
from habit_tracker.models import HabitEntry


def test_import_entries_stores_explicit_zero_score(tmp_path):
    db_path = tmp_path / "habit_tracker.db"

    entries = [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=0.0,
            note="Reviewed and not done",
        ),
    ]

    import_entries(entries, db_path)

    with sqlite3.connect(db_path) as conn:
        rows = conn.execute(
            """
            SELECT date, habit, score, note
            FROM habit_entries
            """
        ).fetchall()

    assert rows == [
        (
            "2025-08-18",
            "Habit review",
            0.0,
            "Reviewed and not done",
        ),
    ]


def test_import_entries_preserves_note_text_with_commas_and_quotes(tmp_path):
    db_path = tmp_path / "habit_tracker.db"

    entries = [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=1.0,
            note='Felt tired, wrote "minimum done"',
        ),
    ]

    import_entries(entries, db_path)

    with sqlite3.connect(db_path) as conn:
        rows = conn.execute(
            """
            SELECT note
            FROM habit_entries
            """
        ).fetchall()

    assert rows == [
        ('Felt tired, wrote "minimum done"',),
    ]


def test_import_entries_skips_duplicates(tmp_path):
    db_path = tmp_path / "habit_tracker.db"

    entries = [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=1.0,
            note="First import",
        ),
    ]

    import_entries(entries, db_path)
    import_entries(entries, db_path)

    with sqlite3.connect(db_path) as conn:
        rows = conn.execute(
            """
            SELECT date, habit, score, note
            FROM habit_entries
            """
        ).fetchall()

    assert rows == [
        (
            "2025-08-18",
            "Habit review",
            1.0,
            "First import",
        ),
    ]


def test_import_entries_skips_entries_older_than_latest_entry(tmp_path):
    db_path = tmp_path / "habit_tracker.db"

    newer_entries = [
        HabitEntry(
            entry_date=date(2025, 8, 19),
            habit="Habit review",
            score=2.0,
            note="Already imported",
        ),
    ]
    older_entries = [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=3.0,
            note="Should be skipped",
        ),
    ]

    import_entries(newer_entries, db_path)
    import_entries(older_entries, db_path)

    with sqlite3.connect(db_path) as conn:
        rows = conn.execute(
            """
            SELECT date, habit, score, note
            FROM habit_entries
            ORDER BY date, habit
            """
        ).fetchall()

    assert rows == [
        (
            "2025-08-19",
            "Habit review",
            2.0,
            "Already imported",
        ),
    ]