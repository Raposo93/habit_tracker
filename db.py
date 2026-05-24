import csv
import sqlite3
from pathlib import Path

from logger import logger


def _create_tables(db_path: Path) -> None:
    db_path.parent.mkdir(parents=True, exist_ok=True)
    with sqlite3.connect(db_path) as conn:
        cursor = conn.cursor()
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS habit_entries (
                date TEXT NOT NULL,
                habit TEXT NOT NULL,
                score REAL,
                note TEXT,
                PRIMARY KEY (date, habit)
            )
        ''')
        conn.commit()
        logger.info("Table 'habit_entries' created or already exists")


def _parse_score(value: str) -> float | None:
    value = value.strip()

    if not value:
        return None

    return float(value.replace(",", "."))


def _entry_exists(cursor: sqlite3.Cursor, date: str, habit: str) -> bool:
    cursor.execute(
        "SELECT 1 FROM habit_entries WHERE date = ? AND habit = ?",
        (date, habit),
    )
    return cursor.fetchone() is not None


def _get_latest_entry_date(cursor: sqlite3.Cursor) -> str | None:
    cursor.execute("SELECT MAX(date) FROM habit_entries")
    return cursor.fetchone()[0]


def import_csv_to_database(csv_path: Path, db_path: Path) -> None:
    _create_tables(db_path)
    with sqlite3.connect(db_path) as conn:
        cursor = conn.cursor()

        with open(csv_path, newline="", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row in reader:
                date = row["date"]
                habit = row["habit"]
                score = _parse_score(row["score"])
                note = row["note"]

                if _entry_exists(cursor, date, habit):
                    logger.info(f"Skipped duplicate: {date} - {habit}")
                    continue

                latest_entry_date = _get_latest_entry_date(cursor)

                if latest_entry_date is not None and date < latest_entry_date:
                    logger.info(
                        f"Skipped older entry: {date} < {latest_entry_date} ({habit})"
                        )
                    continue

                cursor.execute(
                    "INSERT INTO habit_entries (date, habit, score, note) VALUES (?, ?, ?, ?)",
                    (date, habit, score, note),
                )
                logger.info(f"Inserted: {date} - {habit} - {score} - {note}")

        conn.commit()
        logger.info("CSV import completed and committed")
