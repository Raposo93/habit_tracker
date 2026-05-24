import habit_tracker.db as db
from habit_tracker.auth import get_sheets_service
from habit_tracker.reader import SheetReader
from habit_tracker.config import Config
from habit_tracker.logger import logger


def main():
    config = Config()
    logger.info("Starting habit tracker import")

    sheets_service = get_sheets_service()
    reader = SheetReader(sheets_service, config.SPREADSHEET_ID)
    entries = reader.read_week_entries()
    db.import_entries(entries, config.DB_PATH)
    logger.info("Data imported successfully into SQLite")


if __name__ == "__main__":
    main()
