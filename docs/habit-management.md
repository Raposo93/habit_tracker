# Habit management rules for 0.4

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

## Google Sheets import compatibility

The current Google Sheets import resolves habits by exact name.

After renaming a habit, future imports require the corresponding habit name in Sheets to match the new current name.

Version 0.4 does not introduce name aliases, historical names or automatic Sheets header migration.

## Responsibilities by layer

- `domain`: preserve invariants that belong to `Habit` itself.
- `application`: implement create, rename, activate and deactivate use cases.
- `infrastructure`: persist habit state and enforce storage constraints such as unique names.
- `web`: expose HTTP contracts, DTOs and presentation-facing errors.

The concrete write operations are implemented in their dedicated 0.4 issues rather than as part of these rules.
