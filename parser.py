import csv
from typing import List, Dict
from datetime import datetime

def sanitize(text: str) -> str:
    return text.replace('\n', ' ').replace('\r', ' ').strip()

def parse_weekly_data_to_csv(
    habit_names: List[str],
    week_dates: List[str],
    weekly_data: List[Dict],
    csv_path: str
):
    matrix = {}
    for cell in weekly_data:
        key = (cell['row'], cell['col'])
        matrix[key] = cell

    with open(csv_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["fecha", "habito", "valor", "nota"])

        for row_idx, fecha in enumerate(week_dates):
            try:
                fecha_iso = datetime.strptime(fecha, "%d/%m/%Y").strftime("%Y-%m-%d")
            except ValueError:
                fecha_iso = fecha

            for col_idx, habito in enumerate(habit_names):
                cell = matrix.get((row_idx, col_idx), {'value': '', 'note': ''})
                valor = sanitize(str(cell.get('value', '')))
                nota = sanitize(str(cell.get('note', '')))
                writer.writerow([fecha_iso, habito, valor, nota])
