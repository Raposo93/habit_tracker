from pathlib import Path
import db
from auth import get_sheets_service
from reader import SheetReader
from parser import WeeklyDataExporter
from config import Config
from logger import logger


def main():
    config = Config()
    logger.info("Starting habit tracker import")

    sheets_service = get_sheets_service()
    reader = SheetReader(sheets_service, config.SPREADSHEET_ID)

    habit_names = reader.read_habit_names()
    week_dates = reader.read_week_dates()
    weekly_data = reader.read_weekly_data()

    exporter = WeeklyDataExporter(habit_names, week_dates, weekly_data)
    exporter.export_to_csv(config.CSV_OUTPUT)
    logger.info(f"Exported CSV to {config.CSV_OUTPUT}")

    db.import_csv_to_db(config.CSV_OUTPUT)
    logger.info("Data imported successfully into SQLite")


if __name__ == "__main__":
    main()
