# Habit Tracker

Habit Tracker is a Java and React application for recording, storing and analysing habit entries. It supports browser-based daily entry and correction backed by SQLite, together with the existing CLI workflow for Google Sheets imports and reports.

## Current status

Version 0.3.0 replaces manual daily entry and correction in Google Sheets with a write-first browser workflow while preserving the existing CLI import and reporting capabilities.

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
* create habit entries for any selected date from the browser
* correct existing scores and notes after explicit confirmation
* prevent creation from overwriting an existing entry
* prevent correction from creating a missing entry

## Requirements

* Java 21
* Maven
* Node.js 20.19+ or 22.12+

Google Sheets credentials and the `SPREADSHEET_ID` environment variable are required only when importing from Google Sheets. They are not required for the web entry workflow.

## Configuration

Required only for Google Sheets imports:

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

The web workflow uses the SQLite database configured through `DB_PATH`. That database must already contain the active habits to display. Habit management is outside version 0.3.

## Web daily entry

Start the Spring Boot backend from the project root:

```bash
mvn spring-boot:run
```

In a second terminal, start the frontend development server:

```bash
cd frontend
npm ci
npm run dev
```

Open `http://localhost:5173`. The frontend development server proxies `/api` requests to the backend at `http://localhost:8080`.

### Entry and correction workflow

1. Select the date to work on. The page loads the active habits and any entries already recorded for that date.
2. A habit marked `Sin entrada` has no stored entry. Saving it creates a new entry.
3. A habit marked `Registrado` shows its stored score and note. Saving it starts a correction.
4. Before a correction is sent, the frontend shows the stored and proposed values and asks for explicit confirmation.
5. Cancelling the confirmation performs no update. Failed saves keep the entered score and note so they can be reviewed or retried safely.
6. After a successful save, the page reloads the selected date so the visible context reflects the stored data.

If a save may have completed but the latest context cannot be reloaded, the page keeps the visible context marked as stale. Further writes are blocked until the context is loaded successfully using `Reintentar carga`.

Previous dates can be selected for retrospective entry and correction. The same create, correction and confirmation rules apply.

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

Report endpoints are read-only. The daily entry API provides contextual reads together with explicit create and update operations.

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

Create a missing entry:

```text
POST /api/entries/{date}/{habitId}
```

Correct an existing entry:

```text
PUT /api/entries/{date}/{habitId}
```

Both operations accept the same request body:

```json
{
  "score": 2.5,
  "note": "Good progress"
}
```

Create and update have deliberately different semantics. `POST` returns a conflict instead of overwriting an existing habit/date entry, while `PUT` returns not found instead of creating a missing entry. Confirmation before correction belongs to the frontend; it is not represented by a backend confirmation flag.

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

The web entry form also supports half-point scores between these values.

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

The browser workflow replaces manual daily entry and ordinary corrections in Google Sheets. Google Sheets remains available as an import source and is not required when entering or correcting data from the browser.

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

Weekly review, frontend reporting and analysis, and full habit management remain outside version 0.3.

## Roadmap

Review and analysis are intentionally outside version 0.3. See [ROADMAP.md](ROADMAP.md) for the current milestone boundaries and future product scope.
