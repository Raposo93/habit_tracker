from datetime import date
from typing import Protocol

from habit_tracker.models import HabitEntry

EntryKey = tuple[date, str]
StoredEntry = tuple[float, str]


class HabitEntryRepository(Protocol):
    def create_tables(self) -> None: ...

    def fetch_latest_entry_date(self) -> date | None: ...

    def fetch_entries_between_dates(
        self,
        start_date: str,
        end_date: str,
    ) -> dict[EntryKey, StoredEntry]: ...

    def insert_entries(self, entries: list[HabitEntry]) -> None: ...

    def update_entries(self, entries: list[HabitEntry]) -> None: ...