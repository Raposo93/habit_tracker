import sqlite3
from datetime import date
from typing import cast

import pytest

from habit_tracker.db import (
    create_tables,
    fetch_entries_between_dates,
    fetch_latest_entry_date,
    insert_entries,
    update_entries,
)
from habit_tracker.models import HabitEntry


def test_insert_entries_stores_explicit_zero_score(tmp_path):
    db_path = tmp_path / "habit_tracker.db"
    create_tables(db_path)

    entries = [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=0.0,
            note="Reviewed and not done",
        ),
    ]

    insert_entries(entries, db_path)

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


def test_insert_entries_preserves_note_text_with_commas_and_quotes(tmp_path):
    db_path = tmp_path / "habit_tracker.db"
    create_tables(db_path)

    entries = [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=1.0,
            note='Felt tired, wrote "minimum done"',
        ),
    ]

    insert_entries(entries, db_path)

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


def test_insert_entries_rejects_null_score(tmp_path):
    db_path = tmp_path / "habit_tracker.db"
    create_tables(db_path)

    entries = [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=cast(float, None),
            note="Invalid entry",
        ),
    ]

    with pytest.raises(sqlite3.IntegrityError):
        insert_entries(entries, db_path)

    with sqlite3.connect(db_path) as conn:
        rows = conn.execute(
            """
            SELECT date, habit, score, note
            FROM habit_entries
            """
        ).fetchall()

    assert rows == []


def test_fetch_latest_entry_date_returns_latest_date(tmp_path):
    db_path = tmp_path / "habit_tracker.db"
    create_tables(db_path)

    entries = [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=1.0,
            note="First",
        ),
        HabitEntry(
            entry_date=date(2025, 8, 20),
            habit="Exercise",
            score=3.0,
            note="Latest",
        ),
    ]

    insert_entries(entries, db_path)

    assert fetch_latest_entry_date(db_path) == date(2025, 8, 20)


def test_fetch_latest_entry_date_returns_none_when_table_is_empty(tmp_path):
    db_path = tmp_path / "habit_tracker.db"
    create_tables(db_path)

    assert fetch_latest_entry_date(db_path) is None


def test_fetch_entries_between_dates_returns_entries_keyed_by_date_and_habit(tmp_path):
    db_path = tmp_path / "habit_tracker.db"
    create_tables(db_path)

    entries = [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=1.0,
            note="Included",
        ),
        HabitEntry(
            entry_date=date(2025, 8, 19),
            habit="Exercise",
            score=3.0,
            note="Also included",
        ),
        HabitEntry(
            entry_date=date(2025, 8, 25),
            habit="Task review",
            score=2.0,
            note="Outside range",
        ),
    ]

    insert_entries(entries, db_path)

    stored_entries = fetch_entries_between_dates(
        db_path=db_path,
        start_date="2025-08-18",
        end_date="2025-08-19",
    )

    assert stored_entries == {
        (date(2025, 8, 18), "Habit review"): (1.0, "Included"),
        (date(2025, 8, 19), "Exercise"): (3.0, "Also included"),
    }


def test_update_entries_updates_score_and_note(tmp_path):
    db_path = tmp_path / "habit_tracker.db"
    create_tables(db_path)

    insert_entries(
        [
            HabitEntry(
                entry_date=date(2025, 8, 18),
                habit="Habit review",
                score=1.0,
                note="Original note",
            ),
        ],
        db_path,
    )

    update_entries(
        [
            HabitEntry(
                entry_date=date(2025, 8, 18),
                habit="Habit review",
                score=3.0,
                note="Updated note",
            ),
        ],
        db_path,
    )

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
            3.0,
            "Updated note",
        ),
    ]