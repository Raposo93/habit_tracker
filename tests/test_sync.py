from datetime import date

from habit_tracker import db
from habit_tracker.models import HabitEntry
from habit_tracker.sync import import_entries


def test_import_entries_updates_existing_entry_when_score_or_note_changes(tmp_path):
    db_path = tmp_path / "habits.db"

    db.create_tables(db_path)
    db.insert_entries(
        [
            HabitEntry(
                entry_date=date(2025, 8, 24),
                habit="Revision de tareas",
                score=1.0,
                note="old note",
            )
        ],
        db_path,
    )

    import_entries(
        [
            HabitEntry(
                entry_date=date(2025, 8, 24),
                habit="Revision de tareas",
                score=2.0,
                note="new note",
            )
        ],
        db_path,
    )

    entries = db.fetch_entries_between_dates(
        db_path,
        "2025-08-24",
        "2025-08-24",
    )

    assert entries[
        (date(2025, 8, 24), "Revision de tareas")
    ] == (2.0, "new note")


def test_import_entries_keeps_existing_entry_when_score_and_note_are_equal(tmp_path):
    db_path = tmp_path / "habits.db"

    entry = HabitEntry(
        entry_date=date(2025, 8, 24),
        habit="Revision de tareas",
        score=2.0,
        note="same note",
    )

    db.create_tables(db_path)
    db.insert_entries([entry], db_path)

    import_entries([entry], db_path)

    entries = db.fetch_entries_between_dates(
        db_path,
        "2025-08-24",
        "2025-08-24",
    )

    assert entries[
        (date(2025, 8, 24), "Revision de tareas")
    ] == (2.0, "same note")
