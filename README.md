# Habit Tracker

Habit Tracker is a Java application for importing, storing and analysing habit entries. It currently supports a CLI workflow backed by Google Sheets and SQLite, together with a read-only HTTP API for reports and daily entry context.

## Current status

Version 0.3.0 introduces the read-only HTTP API while preserving the existing CLI workflow.

The application can:

* import habit entries from Google Sheets
* store imported entries in SQLite
* store habit entries internally by stable `habit_id`
* keep habit names as the external Google Sheets import contract
* query last week
* query a custom date range
* compare the current range with the previous equivalent range
* ignore days before tracking started when calculating report baselines
* show summary, delta and trend per habit
* expose report queries through HTTP
* expose the active habits and recorded entries for a specific date through HTTP

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

## CLI

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

## HTTP API

The current HTTP API is read-only.

Report for last week:

```text
GET /api/reports/last-week
```

Report for a custom date range:

```text
GET /api/reports?startDate=2026-08-01&endDate=2026-08-31
```

Daily entry context:

```text
GET /api/entries/context?date=2026-09-02
```

The daily entry context returns all active habits together with the entry recorded for the requested date, when one exists:

```json
{
  "date": "2026-09-02",
  "habits": [
    {
      "habitId": "sleep",
      "habitName": "Sleep",
      "entry": {
        "score": 0.0,
        "note": ""
      }
    },
    {
      "habitId": "exercise",
      "habitName": "Exercise",
      "entry": null
    }
  ]
}
```

`entry: null` means that no entry was recorded for that habit and date. It is different from an entry whose score is explicitly `0`.

## Report output

The report includes:

* context
* current range
* previous equivalent range
* recorded entries for the current range
* summary per habit
* previous period score
* current period score
* recorded days
* missing evaluable days
* delta
* trend

The previous range is still shown as the full equivalent date range. However, report scoring ignores any previous-range days before tracking started.

## Score scale

```text
0 = bad
1 = weak
2 = acceptable
3 = good
```

Missing data means no entry was recorded. It is not the same as an explicit `0` score.

For period scores, only evaluable days are included in the denominator:

```text
period_score = sum(recorded entry scores) / evaluable days in range
```

An evaluable day is any day on or after the first stored habit entry date.

Report baseline rules:

* days before tracking started are ignored
* missing days after tracking started contribute `0` to `period_score`
* missing days before tracking started do not exist for report scoring
* if the previous range has no evaluable days, the trend is `NO_BASELINE`
* if the previous range has evaluable days but no recorded entries, the previous score is `0`

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

## Development checks

Run the project verification script before committing:

```bash
./scripts/check.sh
```

The script checks Git diffs for whitespace errors and unresolved conflict markers, then runs `mvn verify`.

## Current limitations

Do not manually rename habit names in Google Sheets yet.

Internally, entries are already stored by `habit_id`, but habit creation, renaming, deactivation and ordering are not exposed through the CLI or HTTP API yet.

The HTTP API is currently read-only. Web-based entry editing and full habit management remain outside the current scope.

## Roadmap

Next development will focus on:

* completing the read-only frontend
* expanding the API required by the web workflow
* web-based daily and weekly habit entry
* visual weekly review and editing
* habit management from the UI
* eventually replacing Google Sheets as the main input UI
