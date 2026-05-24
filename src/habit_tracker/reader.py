import contextlib
from typing import List, Any
import logging

from habit_tracker.models import HabitEntry
from habit_tracker.sheet_parser import build_entries
logger = logging.getLogger(__name__)

class SheetReader:
    def __init__(self, sheets_service: Any, spreadsheet_id: str):
        self.sheets = sheets_service
        self.spreadsheet_id = spreadsheet_id
        logger.info("Initialized SheetReader")

    def _read_habit_names(self) -> List[str]:
        logger.info("Reading habit names from sheet")
        result = self.sheets.spreadsheets().values().get(
            spreadsheetId=self.spreadsheet_id,
            range="C1:1"
        ).execute()
        habits = result.get('values', [[]])[0]
        logger.info(f"Found {len(habits)} habits: {habits}")
        return habits

    def _read_week_dates(self) -> List[str]:
        logger.info("Reading week dates from sheet")
        result = self.sheets.spreadsheets().values().get(
            spreadsheetId=self.spreadsheet_id,
            range="B2:B8"
        ).execute()
        dates = result.get('values', [])
        formatted = [row[0] if row else '' for row in dates]
        logger.info(f"Week dates: {formatted}")
        return formatted

    def _read_weekly_data(self, num_habits: int) -> list[dict]:
        logger.info("Reading weekly data from sheet")

        end_column_letter = chr(ord('C') + num_habits - 1)
        range_to_read = f"C2:{end_column_letter}8"
        logger.info(f"Calculated range to read: {range_to_read}")

        response = self.sheets.spreadsheets().get(
            spreadsheetId=self.spreadsheet_id,
            ranges=[range_to_read],
            includeGridData=True
        ).execute()

        weekly_data = []

        with contextlib.suppress(KeyError, IndexError):
            grid_data = response['sheets'][0]['data'][0]['rowData']
            for row_idx, row in enumerate(grid_data):
                for col_idx, cell in enumerate(row.get('values', [])):
                    value = cell.get('formattedValue', '')
                    note = cell.get('note', '')
                    weekly_data.append({
                        'row': row_idx,
                        'col': col_idx,
                        'value': value,
                        'note': note
                    })
        logger.info(f"Parsed {len(weekly_data)} weekly data entries")
        return weekly_data


    def read_week_entries(self) -> List[HabitEntry]:
        logger.info("Reading week entries from sheet")
        habit_names = self._read_habit_names()

        if not habit_names:
            logger.warning("No habit names found")
            return []

        dates = self._read_week_dates()
        weekly_data = self._read_weekly_data(len(habit_names))

        return build_entries(habit_names, dates, weekly_data)
