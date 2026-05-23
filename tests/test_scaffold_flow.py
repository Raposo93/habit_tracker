from pathlib import Path

from weekly_exporter import WeeklyDataExporter


def test_weekly_data_is_exported_to_sqlite_ready_csv(tmp_path):
    csv_path = tmp_path / "habits_export.csv"

    habit_names = [
        "Revisión de habitos",
        "Recoger",
        "Revision de tareas",
    ]

    week_dates = [
        "18/08/2025",
        "19/08/2025",
    ]

    weekly_data = [
        {"row": 0, "col": 0, "value": "1", "note": "nota revision lunes"},
        {"row": 0, "col": 1, "value": "3", "note": "nota recoger lunes"},
        {"row": 0, "col": 2, "value": "1", "note": ""},
        {"row": 1, "col": 0, "value": "2", "note": "nota revision martes"},
        {"row": 1, "col": 1, "value": "2", "note": ""},
        {"row": 1, "col": 2, "value": "1", "note": "nota tareas martes"},
    ]

    exporter = WeeklyDataExporter(habit_names, week_dates, weekly_data)

    exporter.export_to_csv(csv_path)

    assert csv_path.read_text(encoding="utf-8").splitlines() == [
        "date,habit,score,note",
        "2025-08-18,Revisión de habitos,1,nota revision lunes",
        "2025-08-18,Recoger,3,nota recoger lunes",
        "2025-08-18,Revision de tareas,1,",
        "2025-08-19,Revisión de habitos,2,nota revision martes",
        "2025-08-19,Recoger,2,",
        "2025-08-19,Revision de tareas,1,nota tareas martes",
    ]

import csv
import sqlite3
from pathlib import Path

from db import import_csv_to_database


def test_import_csv_to_database_keeps_expected_output(tmp_path):
    csv_path = Path("tests/resources/habits_export.csv")
    db_path = tmp_path / "habit_tracker.db"

    import_csv_to_database(csv_path, db_path)

    with sqlite3.connect(db_path) as conn:
        rows = conn.execute(
            """
            SELECT date, habit, score, note
            FROM habit_entries
            ORDER BY date, habit
            """
        ).fetchall()

    expected_rows = _read_expected_rows_from_csv(csv_path)

    assert rows == expected_rows


def _read_expected_rows_from_csv(csv_path):
    with open(csv_path, newline="", encoding="utf-8") as file:
        reader = csv.DictReader(file)

        rows = [
            (
                row["date"],
                row["habit"],
                float(row["score"].replace(",", ".")) if row["score"] else None,
                row["note"],
            )
            for row in reader
        ]

    return sorted(rows, key=lambda row: (row[0], row[1]))