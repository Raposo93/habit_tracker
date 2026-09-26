# Habit Tracker

<p align="center">
  <img src="frontend/public/assets/ramon.png" alt="Ramón, the Habit Tracker pixel-art ram mascot" width="180">
</p>

Habit Tracker is a Java and React application for recording, storing and analysing habit entries. It supports browser-based habit management, daily entry and correction backed by SQLite, together with CLI and HTTP reporting.

Ramón, the pixel-art ram above, is the project mascot.

## Current status

The latest released version is 0.3.0. Current development adds habit management for 0.4.0 and retires Google Sheets import support.

The application can:

* create, rename, activate, deactivate and order habits from the browser
* store habit entries in SQLite
* store habit entries internally by stable `habit_id`
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

## Configuration

The optional `DB_PATH` environment variable selects the SQLite database:

```bash
export DB_PATH="db/habit_tracker.db"
```

The default path is `db/habit_tracker.db`. The web application and CLI reports
use the same storage configuration. An empty database can be populated by
creating habits and recording entries from the browser.

Google Sheets import and OAuth are no longer supported. Existing SQLite data
remains available; no re-import or data migration is required for this removal.
Old credentials and token files are not read or deleted by the application.

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

### Habit management workflow

Use the navigation buttons to switch between `Registro diario` and `Hábitos`.
The selected working date is preserved when returning to daily entry, and its
context is reloaded to reflect habit changes.

- Create a habit with a name and daily or weekly cadence. New habits are active.
- Rename an existing habit with `Renombrar`, then save or cancel the edit.
- Use `Desactivar` to hide a habit from daily entry while preserving its history.
- Use `Reactivar` to restore the same habit to daily entry.
- Active and inactive habits remain visible in the management catalog.
- Use `Subir` and `Bajar` to set their order. Daily entry follows the same order,
  showing only active habits. New habits are appended to the end.

The catalog reloads after each successful write. While saving, other writes and
navigation are disabled. Validation errors keep the entered name. If a write
result cannot be confirmed or the subsequent reload fails, reload the catalog
before saving again.

Cadence editing, permanent deletion and scoring guides are not part of this
screen yet.

## CLI

Build and verify the application once before using the CLI:

```bash
mvn test
```

Repeat this step after changing Java code. The `habit` script runs the compiled
CLI through Maven without rebuilding it for each query.

Query last week:

```bash
./habit --query-last-week
```

Query between dates:

```bash
./habit --query-between-dates 2026-05-25 2026-06-07
```

Run `./habit` without arguments to display the available commands.

The script can also be invoked by its path from another directory. It runs from
the project root, so the default database and relative `DB_PATH` values are
resolved there. An absolute `DB_PATH` selects the same database from any location:

```bash
DB_PATH="/absolute/path/habits.db" ./habit --query-last-week
```

Java and Maven must be available on `PATH`. The script reports missing Maven or
compiled classes and returns the Maven process exit status.

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
* `display_order`
* `scoring_guide` (optional free text)

Supported habit cadences:

```text
DAILY
WEEKLY
```

Reports still display habit names, not internal ids.

## Development checks

Run the project verification script before committing:

```bash
./scripts/check.sh
```

The script checks Git diffs for whitespace errors and unresolved conflict markers, then runs `mvn verify`.

Scoring guides are free text reference notes. They preserve line breaks and do
not validate scores or change report calculations. Reports expose the current
guide for habits recorded in either compared period, including inactive habits.
The CLI prints these guides after the report tables.

## Current limitations

The browser supports habit creation, renaming, activation/deactivation and
ordering, and optional scoring guides. Permanent deletion and cadence editing
are not available. The CLI provides reports; habit management and entry/correction use
the browser.

Weekly review, frontend reporting and analysis remain future roadmap work.
Google Sheets import is no longer supported, and no replacement import format
is provided.

## Roadmap

See [ROADMAP.md](ROADMAP.md) for current milestone boundaries and future product
scope.
