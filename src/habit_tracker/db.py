import sqlite3
from datetime import date
from pathlib import Path

from habit_tracker.models import HabitEntry

EntryKey = tuple[date, str]
StoredEntry = tuple[float, str]


def create_tables(db_path: Path) -> None:
    db_path.parent.mkdir(parents=True, exist_ok=True)

    with sqlite3.connect(db_path) as conn:
        cursor = conn.cursor()
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS habit_entries (
                date TEXT NOT NULL,
                habit TEXT NOT NULL,
                score REAL NOT NULL,
                note TEXT,
                PRIMARY KEY (date, habit)
            )
            """
        )
        conn.commit()


def fetch_latest_entry_date(db_path: Path) -> date | None:
    with sqlite3.connect(db_path) as conn:
        cursor = conn.cursor()
        cursor.execute("SELECT MAX(date) FROM habit_entries")
        value = cursor.fetchone()[0]

    if value is None:
        return None

    return date.fromisoformat(value)


def insert_entries(entries: list[HabitEntry], db_path: Path) -> None:
    rows = [
        (
            entry.entry_date.isoformat(),
            entry.habit,
            entry.score,
            entry.note,
        )
        for entry in entries
    ]

    if not rows:
        return

    with sqlite3.connect(db_path) as conn:
        cursor = conn.cursor()
        cursor.executemany(
            """
            INSERT INTO habit_entries (date, habit, score, note)
            VALUES (?, ?, ?, ?)
            """,
            rows,
        )
        conn.commit()


def fetch_entries_between_dates(
    db_path: Path,
    start_date: str,
    end_date: str,
) -> dict[EntryKey, StoredEntry]:
    with sqlite3.connect(db_path) as conn:
        cursor = conn.cursor()
        cursor.execute(
            """
            SELECT date, habit, score, note
            FROM habit_entries
            WHERE date BETWEEN ? AND ?
            """,
            (start_date, end_date),
        )

        rows = cursor.fetchall()

    return {
        (date.fromisoformat(row_date), habit): (score, note)
        for row_date, habit, score, note in rows
    }

def update_entries(entries: list[HabitEntry], db_path: Path) -> None:
    rows = [
        (
            entry.score,
            entry.note,
            entry.entry_date.isoformat(),
            entry.habit,
        )
        for entry in entries
    ]

    if not rows:
        return

    with sqlite3.connect(db_path) as conn:
        cursor = conn.cursor()
        cursor.executemany(
            """
            UPDATE habit_entries
            SET score = ?, note = ?
            WHERE date = ? AND habit = ?
            """,
            rows,
        )
        conn.commit()
