from datetime import date

from habit_tracker.models import HabitEntry
from habit_tracker.sqlite_repository import SqliteHabitEntryRepository


def test_fetch_latest_entry_date_returns_none_when_repository_is_empty(tmp_path):
    db_path = tmp_path / "habit_tracker.db"
    repo = SqliteHabitEntryRepository(db_path)

    repo.create_tables()

    assert repo.fetch_latest_entry_date() is None

def test_insert_entries_and_fetch_latest_entry_date(tmp_path):
    db_path = tmp_path / "habit_tracker.db"
    repository = SqliteHabitEntryRepository(db_path)
    repository.create_tables()

    entries = [
        HabitEntry(
            entry_date=date(2026, 5, 24),
            habit="Reading",
            score=2.0,
            note="Good",
        ),
        HabitEntry(
            entry_date=date(2026, 5, 25),
            habit="Exercise",
            score=3.0,
            note="Strong day",
        ),
    ]

    repository.insert_entries(entries)

    assert repository.fetch_latest_entry_date() == date(2026, 5, 25)


def test_fetch_entries_between_dates(tmp_path):
    db_path = tmp_path / "habit_tracker.db"
    repository = SqliteHabitEntryRepository(db_path)
    repository.create_tables()

    entries = [
        HabitEntry(
            entry_date=date(2026, 5, 23),
            habit="Reading",
            score=1.0,
            note="Outside range",
        ),
        HabitEntry(
            entry_date=date(2026, 5, 24),
            habit="Reading",
            score=2.0,
            note="Good",
        ),
        HabitEntry(
            entry_date=date(2026, 5, 25),
            habit="Exercise",
            score=3.0,
            note="Strong day",
        ),
    ]

    repository.insert_entries(entries)

    stored_entries = repository.fetch_entries_between_dates(
        start_date="2026-05-24",
        end_date="2026-05-25",
    )

    assert stored_entries == {
        (date(2026, 5, 24), "Reading"): (2.0, "Good"),
        (date(2026, 5, 25), "Exercise"): (3.0, "Strong day"),
    }


def test_update_entries_updates_existing_entries(tmp_path):
    db_path = tmp_path / "habit_tracker.db"
    repository = SqliteHabitEntryRepository(db_path)
    repository.create_tables()

    repository.insert_entries(
        [
            HabitEntry(
                entry_date=date(2026, 5, 25),
                habit="Reading",
                score=1.0,
                note="Initial note",
            )
        ]
    )

    repository.update_entries(
        [
            HabitEntry(
                entry_date=date(2026, 5, 25),
                habit="Reading",
                score=3.0,
                note="Updated note",
            )
        ]
    )

    stored_entries = repository.fetch_entries_between_dates(
        start_date="2026-05-25",
        end_date="2026-05-25",
    )

    assert stored_entries == {
        (date(2026, 5, 25), "Reading"): (3.0, "Updated note"),
    }