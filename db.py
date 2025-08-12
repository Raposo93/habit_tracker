# db.py
import sqlite3
from pathlib import Path
import csv
from logger import logger

DB_PATH = Path("habit_tracker.db")

def create_table():
    with sqlite3.connect(DB_PATH) as conn:
        c = conn.cursor()
        c.execute('''
            CREATE TABLE IF NOT EXISTS habitos (
                fecha TEXT NOT NULL,
                habito TEXT NOT NULL,
                valor REAL,
                nota TEXT,
                PRIMARY KEY (fecha, habito)
            )
        ''')
        conn.commit()
        logger.info("Table 'habitos' created or already exists")

def import_csv_to_db(csv_path: str):
    create_table()
    with sqlite3.connect(DB_PATH) as conn:
        cursor = conn.cursor()

        with open(csv_path, newline="", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row in reader:
                fecha = row['fecha']
                habito = row['habito']
                valor = float(row['valor'].replace(",", ".")) if row['valor'] else None
                nota = row['nota']

                cursor.execute(
                    "SELECT 1 FROM habitos WHERE fecha = ? AND habito = ?",
                    (fecha, habito)
                )
                if cursor.fetchone():
                    logger.info(f"Skipped duplicate: {fecha} - {habito}")
                    continue

                cursor.execute("SELECT MAX(fecha) FROM habitos")
                max_fecha = cursor.fetchone()[0]
                if max_fecha and fecha < max_fecha:
                    logger.info(f"Skipped older entry: {fecha} < {max_fecha} ({habito})")
                    continue

                cursor.execute(
                    "INSERT INTO habitos (fecha, habito, valor, nota) VALUES (?, ?, ?, ?)",
                    (fecha, habito, valor, nota)
                )
                logger.info(f"Inserted: {fecha} - {habito} - {valor} - {nota}")

        conn.commit()
        logger.info("CSV import completed and committed")
