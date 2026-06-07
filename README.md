# Habit Tracker

Small CLI habit tracker that imports entries from Google Sheets into SQLite and generates habit reports for weeks or custom date ranges.

## Current status

Version 0.1 focuses on making the CLI usable.

It can:

- import habit entries from Google Sheets
- query last week
- query a custom date range
- compare the current range with the previous equivalent range
- show summary, delta and trend per habit
- store imported entries in SQLite

## Requirements

- Java 21
- Maven
- Google Sheets credentials
- `SPREADSHEET_ID` environment variable

## Configuration

Required environment variable:

```bash
export SPREADSHEET_ID="your-google-sheet-id"
```

The SQLite database path is defined by the application configuration.

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

- context
- current range
- previous range
- entries
- summary
- average score
- recorded days
- missing days
- delta
- trend

## Score scale

```text
0 = bad
1 = weak
2 = acceptable
3 = good
```

Missing data means no entry was recorded. It is not treated as zero.

## Roadmap

Next versions will focus on:

- stable habit identity
- habit renaming without breaking history
- habit cadence
- read-only frontend reports
- eventually replacing Google Sheets as the main input UI
