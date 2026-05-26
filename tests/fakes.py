from datetime import date

from habit_tracker.models import HabitEntry
from habit_tracker.repository import EntryKey, StoredEntry


class FakeHabitEntryRepository:
    def __init__(self):
        self.entries: dict[EntryKey, StoredEntry] = {}
        self.tables_created = False
        self.inserted_entries: list[HabitEntry] = []
        self.updated_entries: list[HabitEntry] = []

    def create_tables(self) -> None:
        self.tables_created = True

    def fetch_latest_entry_date(self) -> date | None:
        if not self.entries:
            return None

        return max(entry_date for entry_date, _habit in self.entries)

    def fetch_entries_between_dates(
        self,
        start_date: str,
        end_date: str,
    ) -> dict[EntryKey, StoredEntry]:
        start = date.fromisoformat(start_date)
        end = date.fromisoformat(end_date)

        return {
            key: value
            for key, value in self.entries.items()
            if start <= key[0] <= end
        }

    def insert_entries(self, entries: list[HabitEntry]) -> None:
        self.inserted_entries.extend(entries)

        for entry in entries:
            self.entries[(entry.entry_date, entry.habit)] = (
                entry.score,
                entry.note,
            )

    def update_entries(self, entries: list[HabitEntry]) -> None:
        self.updated_entries.extend(entries)

        for entry in entries:
            self.entries[(entry.entry_date, entry.habit)] = (
                entry.score,
                entry.note,
            )