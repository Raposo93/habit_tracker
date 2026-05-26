from datetime import date
from pathlib import Path

from habit_tracker import db
from habit_tracker.models import HabitEntry
from habit_tracker.repository import EntryKey, StoredEntry


class SqliteHabitEntryRepository:
    def __init__(self, db_path: Path):
        self.db_path = db_path

    def create_tables(self) -> None:
        db.create_tables(self.db_path)

    def fetch_latest_entry_date(self) -> date | None:
        return db.fetch_latest_entry_date(self.db_path)

    def fetch_entries_between_dates(
        self,
        start_date: str,
        end_date: str,
    ) -> dict[EntryKey, StoredEntry]:
        return db.fetch_entries_between_dates(
            self.db_path,
            start_date,
            end_date,
        )

    def insert_entries(self, entries: list[HabitEntry]) -> None:
        db.insert_entries(entries, self.db_path)

    def update_entries(self, entries: list[HabitEntry]) -> None:
        db.update_entries(entries, self.db_path)