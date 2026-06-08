# Habit Tracker

Small CLI habit tracker that imports entries from Google Sheets into SQLite and generates habit reports for weeks or custom date ranges.

## Current status

Version 0.2 focuses on stable habit identity.

The CLI can:

* import habit entries from Google Sheets
* store imported entries in SQLite
* store habit entries internally by stable `habit_id`
* keep habit names as the external Google Sheets import contract
* query last week
* query a custom date range
* compare the current range with the previous equivalent range
* show summary, delta and trend per habit

## Requirements

* Java 21
* Maven
* Google Sheets credentials
* `SPREADSHEET_ID` environment variable

## Configuration

Required environment variable:

```bash
export SPREADSHEET_ID="your-google-sheet-id"
```

Optional environment variables:

```bash
export DB_PATH="db/habit_tracker.db"
export CREDENTIALS_PATH="credentials.json"
export TOKENS_DIRECTORY_PATH="tokens"
```

If optional values are not provided, the application uses its default paths.

## Commands

Import entries:

```bash
mvn exec:java -Dexec.mainClass="com.raposo.habittracker.Main" -Dexec.args="--import"
```

Query last week:

```bash
mvn exec:java -Dexec.mainClass="com.raposo.habittracker.Main" -Dexec.args="--query-last-week"
```

Query between dates:

```bash
mvn exec:java -Dexec.mainClass="com.raposo.habittracker.Main" -Dexec.args="--query-between-dates 2026-05-25 2026-06-07"
```

## Report output

The report includes:

* context
* current range
* previous range
* entries
* summary
* period score
* recorded days
* missing days
* delta
* trend

## Score scale

```text
0 = bad
1 = weak
2 = acceptable
3 = good
```

Missing data means no entry was recorded. It is not the same as an explicit `0` score.

For period scores, missing days contribute `0` because the score is calculated as:

```text
period_score = sum(recorded entry scores) / days in range
```

## Habit identity

Habit entries are stored internally by `habit_id`.

The `habits` table stores:

* `id`
* `name`
* `cadence`
* `active`

Supported habit cadences:

```text
DAILY
WEEKLY
```

Reports still display habit names, not internal ids.

## Google Sheets import contract

Google Sheets still uses habit names as the external import contract.

During import:

* Sheet habit names must match existing habit names exactly
* unknown habit names fail the import
* habits are not created automatically
* entries are stored using the matching internal `habit_id`

## Current limitations

Do not manually rename habit names in Google Sheets yet.

Internally, entries are already stored by `habit_id`, but habit creation, renaming, deactivation and ordering are not exposed through the CLI yet.

Stable habit identity is implemented, but full habit management is intentionally outside version 0.2.

## Roadmap

Next versions will focus on:

* read-only frontend reports
* backend/API layer for report queries
* web-based daily and weekly habit entry
* visual weekly review and editing
* habit management from the UI
* eventually replacing Google Sheets as the main input UI
