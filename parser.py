import csv
from typing import List, Dict
from datetime import datetime


class WeeklyDataExporter:
    def __init__(self, habit_names: List[str], week_dates: List[str], weekly_data: List[Dict]):
        self.habit_names = habit_names
        self.week_dates = week_dates
        self.weekly_data = weekly_data
        self.matrix = self._build_matrix()

    @staticmethod
    def _sanitize(text: str) -> str:
        return text.replace('\n', ' ').replace('\r', ' ').strip()

    @staticmethod
    def _format_date(date_str: str) -> str:
        try:
            return datetime.strptime(date_str, "%d/%m/%Y").strftime("%Y-%m-%d")
        except ValueError:
            return date_str

    def _build_matrix(self) -> Dict[tuple, Dict]:
        return {(cell['row'], cell['col']): cell for cell in self.weekly_data}

    def export_to_csv(self, csv_path: str):
        with open(csv_path, "w", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            writer.writerow(["fecha", "habito", "valor", "nota"])

            for row_idx, fecha in enumerate(self.week_dates):
                fecha_iso = self._format_date(fecha)

                for col_idx, habito in enumerate(self.habit_names):
                    cell = self.matrix.get((row_idx, col_idx), {'value': '', 'note': ''})
                    valor = self._sanitize(str(cell.get('value', '')))
                    nota = self._sanitize(str(cell.get('note', '')))

                    # Solo exportar si valor no está vacío ni es 'None'
                    if valor and valor.lower() != 'none':
                        writer.writerow([fecha_iso, habito, valor, nota])
