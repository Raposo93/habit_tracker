from habit_tracker.sync import import_entries
from habit_tracker.auth import get_sheets_service
from habit_tracker.reader import SheetReader
from habit_tracker.config import Config
from habit_tracker.logger import logger
from habit_tracker.sqlite_repository import SqliteHabitEntryRepository


def main():
    config = Config()
    logger.info("Starting habit tracker import")
    repository = SqliteHabitEntryRepository(config.DB_PATH)
    sheets_service = get_sheets_service()
    reader = SheetReader(sheets_service, config.SPREADSHEET_ID)
    entries = reader.read_week_entries()
    import_entries(entries, repository)
    logger.info("Data imported successfully into SQLite")


if __name__ == "__main__":
    main()
