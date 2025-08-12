import contextlib
import logging
from typing import List, Dict, Any

logger = logging.getLogger(__name__)

class SheetReader:
    def __init__(self, sheets_service: Any, spreadsheet_id: str):
        self.sheets = sheets_service
        self.spreadsheet_id = spreadsheet_id
        logger.info("Initialized SheetReader")

    def read_habit_names(self) -> List[str]:
        logger.info("Reading habit names from sheet")
        result = self.sheets.spreadsheets().values().get(
            spreadsheetId=self.spreadsheet_id,
            range="C1:1"
        ).execute()
        habits = result.get('values', [[]])[0]
        logger.info(f"Found {len(habits)} habits: {habits}")
        return habits

    def read_week_dates(self) -> List[str]:
        logger.info("Reading week dates from sheet")
        result = self.sheets.spreadsheets().values().get(
            spreadsheetId=self.spreadsheet_id,
            range="B2:B8"
        ).execute()
        dates = result.get('values', [])
        formatted = [row[0] if row else '' for row in dates]
        logger.info(f"Week dates: {formatted}")
        return formatted

    def read_weekly_data(self) -> List[Dict]:
        logger.info("Reading weekly data from sheet")
        habitos = self.read_habit_names()
        if not habitos:
            logger.warning("No habits found")
            return []

        num_habitos = len(habitos)
        end_column_letter = chr(ord('C') + num_habitos - 1)
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
