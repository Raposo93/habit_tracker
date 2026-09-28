# Release notes

## 0.4.1 — Packaged CLI fixes

- Run `--query-last-week` directly from the packaged JAR.
- Run `--query-between-dates` directly from the packaged JAR.
- Do not start the web application for CLI commands.
- Validate packaged CLI behavior during `mvn verify`.
- Handle invalid CLI arguments gracefully.
- Fix Ramón apple animation frame alignment.

## 0.4.0 — Prepared for release

### Habit management

- Create habits with a stable server-generated identity and daily or weekly cadence.
- Rename habits without breaking historical entries; reads use the current name.
- Deactivate habits without deleting history and restore the same identity later.
- Order the full catalog, including inactive habits. Daily Entry follows that order.
- Store optional multiline scoring guides and create, edit or remove them in the
  browser. Guides are reference text and do not validate or calculate scores.
- Include current guides in API and CLI reports for habits recorded in either
  compared period, including inactive historical habits. Guides are not versioned.

### CLI

- Use `./habit` to invoke the existing report commands and help without typing
  the full Maven invocation. Run `mvn test` first and after changing Java code.
- Render report tables with compact Markdown spacing, preserving values and order.

### Compatibility and storage

- Google Sheets import, the `--import` command and Google OAuth configuration are
  no longer supported. Enter and correct data through the browser.
- `DB_PATH` remains the storage setting shared by web and CLI. Old Google
  credentials and token files are not read or deleted by the application.
- Existing databases automatically gain persisted habit order and optional guide
  metadata. Initial ordering preserves the previous alphabetical order; restarts
  preserve configured positions. Habit identities, scores and notes remain intact.
- Habit creation and catalog JSON now include nullable `scoringGuide`; report JSON
  includes `scoringGuides`. Existing report calculations remain unchanged.
- Missing entries remain different from recorded zeroes. Fractional scores and
  null baseline values retain their existing semantics.

### Limits

Permanent deletion and cadence editing are not supported. Weekly review and
reporting in the frontend remain future work. No replacement import format is
provided.

### Package

`mvn verify` builds `target/habit-tracker-0.4.0.jar` with the frontend bundled.
Run it with Java 21 using `java -jar target/habit-tracker-0.4.0.jar` and open
`http://localhost:8080`. No tag or published release is created by preparing these
notes and metadata.
