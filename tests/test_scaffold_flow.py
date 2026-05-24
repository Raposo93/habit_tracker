import sqlite3
from datetime import date

from habit_tracker.db import import_entries
from habit_tracker.models import HabitEntry


def test_import_entries_stores_habit_entries(tmp_path):
    db_path = tmp_path / "habit_tracker.db"

    entries = [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=1.0,
            note="Monday note",
        ),
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Tidying up",
            score=3.0,
            note="",
        ),
    ]

    import_entries(entries, db_path)

    with sqlite3.connect(db_path) as conn:
        rows = conn.execute(
            """
            SELECT date, habit, score, note
            FROM habit_entries
            ORDER BY date, habit
            """
        ).fetchall()

    assert rows == [
        ("2025-08-18", "Habit review", 1.0, "Monday note"),
        ("2025-08-18", "Tidying up", 3.0, ""),
    ]
