import logging
from pathlib import Path

LOG_PATH = Path("habit_tracker.log")

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s [%(name)s]: %(message)s",
    filename="habit_tracker.log",
    filemode="a",
)

logger = logging.getLogger("habit_tracker")
