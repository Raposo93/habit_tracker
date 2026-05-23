from pathlib import Path
from dotenv import load_dotenv # type: ignore
import os


class Config:
    def __init__(self, env_path: Path = Path(".env")):
        load_dotenv(dotenv_path=env_path)

        self.CREDENTIALS_PATH = Path(os.getenv("CREDENTIALS_PATH", "credentials.json"))
        self.TOKEN_PATH: Path = Path(os.getenv("TOKEN_PATH", "token.json"))
        self.CSV_OUTPUT = Path(os.getenv("CSV_OUTPUT", "habits_export.csv"))
        self.DB_PATH = Path(os.getenv("DB_PATH", "./db/habit_tracker.db"))
        self.SPREADSHEET_ID: str = os.getenv("SPREADSHEET_ID") or ""
        if not self.SPREADSHEET_ID:
            raise ValueError("SPREADSHEET_ID it is not configured on .env")

