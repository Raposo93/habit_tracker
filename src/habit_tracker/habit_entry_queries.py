from datetime import date, timedelta

from habit_tracker.repository import HabitEntryRepository, EntryKey, StoredEntry


def get_entries_between_dates(
    repo: HabitEntryRepository,
    start_date: date,
    end_date: date,
) -> dict[EntryKey, StoredEntry]:
    return repo.fetch_entries_between_dates(
        start_date=start_date.isoformat(),
        end_date=end_date.isoformat(),
    )


def get_week_entries(
    repo: HabitEntryRepository,
    reference_date: date,
) -> dict[EntryKey, StoredEntry]:
    start_date = reference_date - timedelta(days=reference_date.weekday())
    end_date = start_date + timedelta(days=6)

    return get_entries_between_dates(repo, start_date, end_date)