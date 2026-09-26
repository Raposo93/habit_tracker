# Habit management in 0.4.0

Version 0.4 introduces habit management without changing the identity model used by entries and reports.

## Identity and naming

- `HabitId` is the stable identity of a habit.
- New habits receive a server-generated UUID as their stable `HabitId`.
- A habit name is editable and must not be used as its identity.
- Habit names remain unique.
- Renaming a habit preserves its `HabitId` and historical entries.
- Reports and daily entry use the current habit name.

## Lifecycle

- New habits are active by default.
- Deactivating a habit preserves its `HabitId` and historical entries.
- Inactive habits do not appear in Daily Entry.
- Reactivating a habit restores the same habit with the same `HabitId`.
- Permanent deletion is not part of 0.4.

## Catalog and creation API

`GET /api/habits` returns `habits`, including active and inactive habits in the
configured order. Each habit contains `habitId`, `habitName`, `cadence`, `active`
and nullable `scoringGuide`.

`POST /api/habits` creates an active habit with a server-generated identity:

```json
{"habitName": "Sleep", "cadence": "DAILY", "scoringGuide": "0: tired\n3: rested"}
```

`scoringGuide` is optional; cadences are `DAILY` or `WEEKLY`. Names are trimmed,
nonblank and unique across active and inactive habits, using case-sensitive
comparison. Success returns `201 Created` with the created habit. Invalid names
or cadences return `400` (`INVALID_HABIT_NAME` or `INVALID_HABIT_CADENCE`);
occupied names return `409` (`HABIT_NAME_ALREADY_EXISTS`).

## Active-state API

`PUT /api/habits/{habitId}/active` sets the desired state with a JSON body:

```json
{"active": false}
```

Use `true` to reactivate the same habit. The operation returns `204 No Content`,
including when the habit already has the requested state. An unknown identity
returns `404` with code `HABIT_NOT_FOUND`. Missing, null or malformed state
requests return `400` with code `INVALID_HABIT`.

The operation changes only the active state. The full catalog still includes
inactive habits, while subsequent Daily Entry context requests exclude them.
Historical entries remain stored, and existing entry creation/correction API
rules are unchanged.

## Rename API

`PUT /api/habits/{habitId}/name` changes the current display name:

```json
{"habitName": "Rest"}
```

Names are trimmed and must not be null or blank. Names remain unique across
active and inactive habits, with the existing case-sensitive comparison.
Renaming to the same name succeeds.

The operation returns `204 No Content`. Invalid names return `400` with code
`INVALID_HABIT_NAME`; unknown identities return `404` with `HABIT_NOT_FOUND`;
occupied names return `409` with `HABIT_NAME_ALREADY_EXISTS`. Malformed JSON
returns `400` with `INVALID_HABIT`.

Renaming preserves identity, cadence, active state and historical scores and
notes. Subsequent Daily Entry context and report queries use the new name,
including for historical entries. No historical display names or aliases are
stored.

Google Sheets compatibility is no longer a supported product requirement.
The Google Sheets import implementation and OAuth configuration have been
removed. Existing SQLite history remains available.

## Habit order

Habit Management and Daily Entry use one persisted order. The management catalog
includes inactive habits in that order; Daily Entry filters them out without
changing the relative order of active habits. Renaming, deactivation and
reactivation preserve positions. New habits are appended to the end.

Existing databases are upgraded automatically: their initial positions preserve
the previous alphabetical order. Subsequent application starts preserve the
configured positions.

Use `Subir` and `Bajar` in the management screen to move a habit one position.
The UI waits for the write and reload before displaying the confirmed order.

`PUT /api/habits/order` accepts the complete catalog, including inactive habits:

```json
{"habitIds": ["sleep-id", "exercise-id", "review-id"]}
```

Each identity must appear exactly once. The operation returns `204 No Content`;
repeating the same order succeeds. An empty list is valid only for an empty
catalog. Missing, blank, null or duplicate identities return `400` with
`INVALID_HABIT_ORDER`. A list that omits a stored habit or includes an unknown
identity returns `409` with `HABIT_CATALOG_CHANGED`; reload before trying again.
The transaction either saves all positions or leaves the existing order intact.

Report ordering remains unchanged.

## Responsibilities by layer

- `domain`: preserve invariants that belong to `Habit` itself.
- `application`: implement create, rename, active-state, ordering and guide use cases.
- `infrastructure`: persist habit state and enforce storage constraints such as unique names.
- `web`: expose HTTP contracts, DTOs and presentation-facing errors.

## Scoring guides

Creation accepts optional `scoringGuide` text; the catalog and creation response
include it as a string or `null`. Existing databases gain a nullable
`scoring_guide` column automatically without changing history or habit order.

`PUT /api/habits/{habitId}/scoring-guide` edits only the guide:

```json
{"scoringGuide": "0: no activity\n3: target completed"}
```

The field is required for this update, but `null`, empty or whitespace-only text
removes the guide. Nonblank text is preserved exactly, including line breaks.
Success returns `204`; unknown identities return `404` (`HABIT_NOT_FOUND`), and
missing fields or malformed requests return `400` (`INVALID_HABIT`).

The browser supports creation, editing, cancellation and removal of guides.
They are reference text only: fractional scores remain valid and no scoring
rules are inferred from the text. Renaming and activation preserve the guide.

Report JSON includes `scoringGuides`, each with `habitId`, `habitName` and
`scoringGuide`, for habits recorded in the current or previous period. Inactive
historical habits are included; habits outside those periods are excluded. Each
habit appears once, using its current name and guide. Guides are not versioned.
The CLI adds a section after its tables only when relevant guides exist.
