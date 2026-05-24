import logging
from datetime import date, datetime
from typing import Any

from habit_tracker.models import HabitEntry

logger = logging.getLogger(__name__)


def build_entries(
    habit_names: list[str],
    dates: list[str],
    weekly_data: list[dict[str, Any]],
) -> list[HabitEntry]:
    entries = []

    for cell in weekly_data:
        row_idx = cell["row"]
        col_idx = cell["col"]

        if row_idx >= len(dates) or col_idx >= len(habit_names):
            continue

        raw_score = str(cell.get("value", ""))

        if not raw_score.strip() or raw_score.strip().lower() == "none":
            continue

        entry = HabitEntry(
            entry_date=_parse_date(dates[row_idx]),
            habit=habit_names[col_idx],
            score=_parse_score(raw_score),
            note=str(cell.get("note", "")).strip(),
        )
        entries.append(entry)

    logger.info(f"Built {len(entries)} habit entries")
    return entries


def _parse_date(value: str) -> date:
    return datetime.strptime(value.strip(), "%d/%m/%Y").date()


def _parse_score(value: str) -> float:
    return float(value.strip().replace(",", "."))
