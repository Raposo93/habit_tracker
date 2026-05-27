from datetime import date

from habit_tracker.models import HabitEntry
from habit_tracker.sync import import_entries
from tests.fakes import FakeHabitEntryRepository


def test_import_entries_updates_existing_entry_when_score_or_note_changes():
    repo = FakeHabitEntryRepository()

    repo.entries[
        (date(2025, 8, 24), "Task review")
    ] = (1.0, "old note")

    import_entries(
        [
            HabitEntry(
                entry_date=date(2025, 8, 24),
                habit="Task review",
                score=2.0,
                note="new note",
            )
        ],
        repo,
    )

    assert repo.updated_entries == [
        HabitEntry(
            entry_date=date(2025, 8, 24),
            habit="Task review",
            score=2.0,
            note="new note",
        )
    ]

    assert repo.entries[
        (date(2025, 8, 24), "Task review")
    ] == (2.0, "new note")


def test_import_entries_keeps_existing_entry_when_score_and_note_are_equal():
    repo = FakeHabitEntryRepository()

    entry = HabitEntry(
        entry_date=date(2025, 8, 24),
        habit="Task review",
        score=2.0,
        note="same note",
    )

    repo.entries[
        (entry.entry_date, entry.habit)
    ] = (entry.score, entry.note)

    import_entries([entry], repo)

    assert repo.inserted_entries == []
    assert repo.updated_entries == []
    assert repo.entries[
        (date(2025, 8, 24), "Task review")
    ] == (2.0, "same note")