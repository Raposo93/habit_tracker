# Roadmap

This roadmap describes the main product milestones for Habit Tracker.

The goal is not to assign a release number to every implementation step.
GitHub Issues track the concrete work required to reach each milestone.

The roadmap may change as real usage reveals more valuable priorities.

---

## 0.1.0 — Core reporting engine

### Goal

Build a reliable core for importing, querying and interpreting habit data.

### Done when

- Data can be imported.
- Reports can be generated for custom date ranges.
- Weekly reports can be generated.
- Habit summaries can be calculated.
- Previous-period comparison works.
- `delta` and `trend` are calculated consistently.
- Missing entries are distinguished from recorded entries.
- A score of `0` is treated as a real recorded value.
- `NO_BASELINE` is represented correctly.

### Out of scope

- Data entry from the frontend.
- Habit management.
- Charts.
- Advanced frontend features.

---

## 0.2.0 — Stable habit identity

### Goal

Give habits a persistent identity that does not depend on their visible name.

This milestone stabilizes the data model before the application starts
creating new data outside Google Sheets.

### Done when

- Habits have their own persistent identity.
- Entries reference habits by `habit_id`.
- Existing historical data can be migrated without losing information.
- A habit can be renamed without breaking its history.
- Accidental duplicate habits are prevented or handled clearly.
- Google Sheets imports resolve habit names to the correct `habit_id`.
- Interactive application flows can use stable habit identity instead of
  relying on the visible habit name.

### Out of scope

- Full habit management UI.
- Daily entry frontend.
- Reporting redesign.
- Charts.

---

## 0.3.0 — Daily entry and correction

### Goal

Replace manual habit entry and correction in Google Sheets.

The UI is write-first. It may read the minimum contextual data required to
write safely.

### Done when

- Any date can be selected.
- Active habits can be loaded for the selected date context.
- Stable `habitId` and visible `habitName` are handled separately.
- Existing entries for the selected date can be loaded.
- Existing score and note values are visible before editing.
- New entries can be created for the selected date.
- Retrospective entries can be created for previous days.
- Existing entries can be corrected.
- Notes can be added, changed or removed.
- Create and update are explicit, separate operations.
- Creating an entry cannot overwrite an existing habit/date entry.
- Updating an entry cannot silently create a missing entry.
- The frontend asks for explicit confirmation before updating an existing
  entry.
- Cancelling the confirmation performs no update.
- Daily habit tracking and ordinary corrections can be performed without
  manually editing Google Sheets.

### Out of scope

- Weekly review.
- Frontend reporting and trend analysis.
- Charts and dashboards.
- Habit management.
- Advanced styling.

---

## 0.4.0 — Habit management

### Goal

Remove the need to edit SQLite, configuration or Google Sheets when the set of
tracked habits changes.

### Done when

- Habits can be created.
- Habits can be renamed.
- Habits can be deactivated or hidden without losing history.
- Inactive habits can be restored.
- Habits can be ordered.
- Renaming a habit preserves its historical identity.
- Existing entries remain linked to the correct habit after habit changes.
- Duplicate habits are prevented or handled explicitly.

### Out of scope

- Advanced analytics.
- Complex goal systems.
- Collaboration between users.
- Social features.

---

## 0.5.0 — Review

### Goal

Review and understand habit data from the application.

This milestone brings the existing reporting capabilities into a read-only
frontend workflow.

### Done when

- A weekly report can be viewed.
- Custom date ranges can be reviewed.
- Entries can be viewed by day.
- Habit summaries are visible.
- Current and previous period scores are visible.
- `delta` and `trend` are displayed.
- `NO_BASELINE` is presented clearly.
- Empty current ranges are distinguished from missing baseline data.
- Recorded and missing days are distinguishable.
- The frontend displays backend business rules instead of recalculating them.
- Missing or relevant data can be identified clearly from the review flow.

Editing remains part of the 0.3 entry and correction capability. Habit
management belongs to 0.4. The 0.5 milestone focuses on consultation and
interpretation.

### Out of scope

- Advanced charts.
- Automated analysis.
- Complex dashboards.

---

## 0.6.0 — Analysis

### Goal

Help interpret habit data rather than only store and display it.

### Possible scope

- Identify improving habits.
- Identify worsening habits.
- Highlight habits with little recorded data.
- Compare months or custom periods.
- Compare against the previous period.
- Highlight meaningful changes.
- Surface useful context from notes where appropriate.

Charts may be introduced when they genuinely improve understanding, but they
are not a requirement by themselves.

### Done when

The application provides useful conclusions that would otherwise require
manual inspection of the underlying data.

---

## 0.7.0 — Reliability and operations

### Goal

Make the application safe and predictable to operate over time.

### Done when

- Data can be backed up.
- Backups can be restored.
- Errors are understandable.
- Useful logs are available.
- Data locations are documented.
- Configuration is documented.
- Installation is documented for supported environments.
- Common failures can be diagnosed without guesswork.
- Recovering from common problems does not require manual database repair or
  undocumented steps.

---

## 1.0.0 — Normal standalone use

### Goal

Habit Tracker can be used normally without depending on Google Sheets or the
terminal.

### Done when

A user can:

- install and configure the application;
- import existing data or start with new data;
- manage habits;
- record daily habit data;
- correct historical entries;
- review weeks and custom periods;
- interpret habit progress;
- understand common errors;
- back up and restore data.

Google Sheets may remain available as an import or interoperability option, but
it is no longer required for normal use.

---

## Versioning policy

Minor versions represent meaningful product capabilities rather than
implementation steps.

Examples:

- `0.3.0` — habit data can be entered and corrected from the application;
- `0.4.0` — tracked habits can be managed from the application;
- `0.5.0` — habit data can be reviewed from the application;
- `0.6.0` — the application helps interpret habit data.

Patch versions are reserved for fixes and small improvements after a release.

Examples:

- `0.3.1` — fix incorrect note overwrite behavior;
- `0.3.2` — fix score `0` validation.

Concrete implementation work is tracked using GitHub Issues. Issue order must
not be represented using fake release numbers such as `0.3.1`, `0.3.2` and
`0.3.3`.
