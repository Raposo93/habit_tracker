from datetime import date

from habit_tracker.models import HabitEntry
from habit_tracker.sync import import_entries
from tests.fakes import FakeHabitEntryRepository


def test_import_entries_updates_existing_entry_when_score_or_note_changes():
    repository = FakeHabitEntryRepository()

    repository.entries[
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
        repository,
    )

    assert repository.updated_entries == [
        HabitEntry(
            entry_date=date(2025, 8, 24),
            habit="Task review",
            score=2.0,
            note="new note",
        )
    ]

    assert repository.entries[
        (date(2025, 8, 24), "Task review")
    ] == (2.0, "new note")


def test_import_entries_keeps_existing_entry_when_score_and_note_are_equal():
    repository = FakeHabitEntryRepository()

    entry = HabitEntry(
        entry_date=date(2025, 8, 24),
        habit="Task review",
        score=2.0,
        note="same note",
    )

    repository.entries[
        (entry.entry_date, entry.habit)
    ] = (entry.score, entry.note)

    import_entries([entry], repository)

    assert repository.inserted_entries == []
    assert repository.updated_entries == []
    assert repository.entries[
        (date(2025, 8, 24), "Task review")
    ] == (2.0, "same note")