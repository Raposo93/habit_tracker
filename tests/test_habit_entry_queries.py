from datetime import date

from habit_tracker.habit_entry_queries import (
    get_entries_between_dates,
    get_week_entries,
)
from tests.fakes import FakeHabitEntryRepository


def test_get_entries_between_dates_returns_entries_in_requested_range():
    repo = FakeHabitEntryRepository()

    repo.entries = {
        (date(2026, 5, 24), "Reading"): (1.0, "Outside range"),
        (date(2026, 5, 25), "Reading"): (2.0, "Included"),
        (date(2026, 5, 26), "Exercise"): (3.0, "Included"),
        (date(2026, 5, 27), "Sleep"): (1.0, "Outside range"),
    }

    entries = get_entries_between_dates(
        repo,
        start_date=date(2026, 5, 25),
        end_date=date(2026, 5, 26),
    )

    assert entries == {
        (date(2026, 5, 25), "Reading"): (2.0, "Included"),
        (date(2026, 5, 26), "Exercise"): (3.0, "Included"),
    }


def test_get_week_entries_returns_monday_to_sunday_entries():
    repo = FakeHabitEntryRepository()

    repo.entries = {
        (date(2026, 5, 24), "Reading"): (1.0, "Previous Sunday"),
        (date(2026, 5, 25), "Reading"): (2.0, "Monday"),
        (date(2026, 5, 27), "Exercise"): (3.0, "Wednesday"),
        (date(2026, 5, 31), "Sleep"): (2.0, "Sunday"),
        (date(2026, 6, 1), "Reading"): (1.0, "Next Monday"),
    }

    entries = get_week_entries(
        repo,
        reference_date=date(2026, 5, 27),
    )

    assert entries == {
        (date(2026, 5, 25), "Reading"): (2.0, "Monday"),
        (date(2026, 5, 27), "Exercise"): (3.0, "Wednesday"),
        (date(2026, 5, 31), "Sleep"): (2.0, "Sunday"),
    }