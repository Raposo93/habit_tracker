from datetime import date

from habit_tracker.models import HabitEntry
from habit_tracker.sheet_parser import build_entries


def test_build_entries_keeps_explicit_zero_and_ignores_empty_or_none():
    habit_names = [
        "Habit review",
        "Tidying up",
        "Task review",
        "Exercise",
    ]
    dates = ["18/08/2025"]

    weekly_data = [
        {
            "row": 0,
            "col": 0,
            "value": "0",
            "note": "Reviewed and not done",
        },
        {
            "row": 0,
            "col": 1,
            "value": "",
            "note": "This note should not be imported",
        },
        {
            "row": 0,
            "col": 2,
            "value": "None",
            "note": "Legacy empty value",
        },
        {
            "row": 0,
            "col": 3,
            "value": "3",
            "note": "Done",
        },
    ]

    entries = build_entries(habit_names, dates, weekly_data)

    assert entries == [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=0.0,
            note="Reviewed and not done",
        ),
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Exercise",
            score=3.0,
            note="Done",
        ),
    ]


def test_build_entries_parses_comma_decimal_score():
    habit_names = ["Habit review"]
    dates = ["18/08/2025"]
    weekly_data = [
        {
            "row": 0,
            "col": 0,
            "value": "2,5",
            "note": " Partial progress ",
        },
    ]

    entries = build_entries(habit_names, dates, weekly_data)

    assert entries == [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=2.5,
            note="Partial progress",
        ),
    ]


def test_build_entries_ignores_cells_outside_known_dates_or_habits():
    habit_names = ["Habit review"]
    dates = ["18/08/2025"]

    weekly_data = [
        {
            "row": 99,
            "col": 0,
            "value": "1",
            "note": "Invalid row",
        },
        {
            "row": 0,
            "col": 99,
            "value": "1",
            "note": "Invalid column",
        },
    ]

    entries = build_entries(habit_names, dates, weekly_data)

    assert entries == []

def test_build_entries_ignores_cell_without_score_even_when_note_exists():
    habit_names = ["Habit review"]
    dates = ["18/08/2025"]

    weekly_data = [
        {
            "row": 0,
            "col": 0,
            "value": "",
            "note": "This note should not create an entry",
        },
    ]

    entries = build_entries(habit_names, dates, weekly_data)

    assert entries == []


def test_build_entries_normalizes_empty_note_to_empty_string():
    habit_names = ["Habit review"]
    dates = ["18/08/2025"]

    weekly_data = [
        {
            "row": 0,
            "col": 0,
            "value": "2",
            "note": "   ",
        },
    ]

    entries = build_entries(habit_names, dates, weekly_data)

    assert entries == [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=2.0,
            note="",
        ),
    ]

def test_build_entries_strips_note():
    habit_names = ["Habit review"]
    dates = ["18/08/2025"]

    weekly_data = [
        {
            "row": 0,
            "col": 0,
            "value": "2",
            "note": "  Some progress made  ",
        },
    ]

    entries = build_entries(habit_names, dates, weekly_data)

    assert entries == [
        HabitEntry(
            entry_date=date(2025, 8, 18),
            habit="Habit review",
            score=2.0,
            note="Some progress made",
        ),
    ]