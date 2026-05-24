from dataclasses import dataclass
from datetime import date


@dataclass(frozen=True)
class HabitEntry:
    entry_date: date
    habit: str
    score: float
    note: str = ""
