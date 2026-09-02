import { useEffect, useId, useState } from "react";

const SCORES = [
  { value: 0, label: "0", accessibleLabel: "0" },
  { value: 0.5, label: "·", accessibleLabel: "0,5" },
  { value: 1, label: "1", accessibleLabel: "1" },
  { value: 1.5, label: "·", accessibleLabel: "1,5" },
  { value: 2, label: "2", accessibleLabel: "2" },
  { value: 2.5, label: "·", accessibleLabel: "2,5" },
  { value: 3, label: "3", accessibleLabel: "3" },
];

function displayScore(score) {
  return score === "" ? "—" : score.replace(".", ",");
}

function confirmationMessage(habitName, storedEntry, draft) {
  const storedNote = storedEntry.note || "Sin nota";
  const draftNote = draft.note || "Sin nota";

  return [
    `Vas a corregir ${habitName}.`,
    "",
    `Actual: ${storedEntry.score} · ${storedNote}`,
    `Nuevo: ${draft.score} · ${draftNote}`,
    "",
    "¿Quieres guardar la corrección?",
  ].join("\n");
}

export default function HabitEntryForm({ habit, onCreate, onUpdate }) {
  const [score, setScore] = useState(
    habit.entry === null ? "" : String(habit.entry.score),
  );
  const [note, setNote] = useState(habit.entry?.note ?? "");
  const [storedNoteExpanded, setStoredNoteExpanded] = useState(false);
  const storedNoteId = useId();
  const storedNote = habit.entry?.note ?? "";
  const canExpandStoredNote =
    storedNote.length > 120 || storedNote.split("\n").length > 4;

  useEffect(() => {
    setScore(habit.entry === null ? "" : String(habit.entry.score));
    setNote(habit.entry?.note ?? "");
    setStoredNoteExpanded(false);
  }, [habit.entry]);

  async function submitEntry(event) {
    event.preventDefault();

    if (score === "") {
      return;
    }

    const draft = {
      score: Number(score),
      note,
    };

    try {
      if (habit.entry === null) {
        await onCreate(habit.habitId, draft);
        return;
      }

      const confirmed = window.confirm(
        confirmationMessage(habit.habitName, habit.entry, draft),
      );

      if (confirmed) {
        await onUpdate(habit.habitId, draft);
      }
    } catch {
      window.alert("No se pudo guardar la entrada.");
    }
  }

  return (
    <article className="habit-card">
      <div className="habit-card__header">
        <div>
          <p className="habit-card__label">Hábito</p>
          <h2>{habit.habitName}</h2>
        </div>
        <span
          className={
            habit.entry === null
              ? "entry-badge entry-badge--empty"
              : "entry-badge"
          }
        >
          {habit.entry === null ? "Sin entrada" : "Registrado"}
        </span>
      </div>

      {habit.entry !== null && (
        <div className="stored-entry" aria-label="Valor almacenado">
          <span>Actual</span>
          <strong>{habit.entry.score}</strong>
          <p
            className={
              canExpandStoredNote && !storedNoteExpanded
                ? "stored-entry__note stored-entry__note--collapsed"
                : "stored-entry__note"
            }
            id={storedNoteId}
          >
            {storedNote || "Sin nota"}
          </p>
          {canExpandStoredNote && (
            <button
              aria-controls={storedNoteId}
              aria-expanded={storedNoteExpanded}
              className="stored-entry__toggle"
              onClick={() => setStoredNoteExpanded((expanded) => !expanded)}
              type="button"
            >
              {storedNoteExpanded ? "Ocultar nota" : "Ver nota completa"}
            </button>
          )}
        </div>
      )}

      <form onSubmit={submitEntry}>
        <fieldset className="score-fieldset">
          <legend>Score</legend>
          <output className="selected-score" aria-label="Score seleccionado">
            {displayScore(score)}
          </output>
          <div className="score-options">
            {SCORES.map((option) => (
              <label
                className={
                  Number.isInteger(option.value)
                    ? "score-option"
                    : "score-option score-option--half"
                }
                key={option.value}
              >
                <input
                  type="radio"
                  name={`score-${habit.habitId}`}
                  value={option.value}
                  checked={score === String(option.value)}
                  onChange={(event) => setScore(event.target.value)}
                  aria-label={option.accessibleLabel}
                  required
                />
                <span aria-hidden="true">{option.label}</span>
              </label>
            ))}
          </div>
          <p className="score-hint">· representa medio punto</p>
        </fieldset>

        <label className="note-field">
          <span>Nota</span>
          <textarea
            value={note}
            onChange={(event) => setNote(event.target.value)}
            placeholder="Opcional"
            rows="3"
          />
        </label>

        <button className="save-button" type="submit">
          {habit.entry === null ? "Guardar entrada" : "Guardar corrección"}
        </button>
      </form>
    </article>
  );
}
