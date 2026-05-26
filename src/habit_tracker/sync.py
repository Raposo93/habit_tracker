from habit_tracker.repository import HabitEntryRepository
from habit_tracker.logger import logger
from habit_tracker.models import HabitEntry


def import_entries(
    entries: list[HabitEntry], 
    repo: HabitEntryRepository,
) -> None:
    
    if not entries:
        logger.info("No entries to import")
        return

    repo.create_tables()

    sorted_entries = sorted(entries, key=lambda item: (item.entry_date, item.habit))

    start_date = sorted_entries[0].entry_date.isoformat()
    end_date = sorted_entries[-1].entry_date.isoformat()

    latest_entry_date = repo.fetch_latest_entry_date()
    existing_entries = repo.fetch_entries_between_dates(
        start_date,
        end_date,
    )

    entries_to_insert: list[HabitEntry] = []
    entries_to_update: list[HabitEntry] = []

    for entry in sorted_entries:
        key = (entry.entry_date, entry.habit)
        new_entry = (entry.score, entry.note)

        stored_entry = existing_entries.get(key)

        if stored_entry is not None:
            if stored_entry != new_entry:
                entries_to_update.append(entry)
                existing_entries[key] = new_entry
                logger.info(f"Queued update: {entry.entry_date} - {entry.habit}")
            else:
                logger.info(f"Skipped duplicate: {entry.entry_date} - {entry.habit}")

            continue

        if latest_entry_date is not None and entry.entry_date < latest_entry_date:
            logger.info(
                f"Skipped older entry: {entry.entry_date} < {latest_entry_date} "
                f"({entry.habit})"
            )
            continue

        entries_to_insert.append(entry)
        existing_entries[key] = new_entry

    repo.insert_entries(entries_to_insert)
    repo.update_entries(entries_to_update)

    logger.info(
        "Entries import completed: "
        f"{len(entries_to_insert)} inserted, "
        f"{len(entries_to_update)} updated"
    )
