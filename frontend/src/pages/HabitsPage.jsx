import { useEffect, useRef, useState } from "react";
import { createHabit, loadHabits, renameHabit, reorderHabits, setHabitActive, setHabitScoringGuide } from "../api/habits.js";

function errorMessage(error) {
  switch (error?.code) {
    case "HABIT_CATALOG_CHANGED": return "El catálogo de hábitos ha cambiado. Recárgalo antes de ordenar de nuevo.";
    case "INVALID_HABIT_ORDER": return "El orden de hábitos no es válido. Recarga el catálogo.";
    case "INVALID_HABIT_NAME": return "El nombre no puede estar vacío.";
    case "HABIT_NAME_ALREADY_EXISTS": return "Ya existe un hábito con ese nombre, aunque esté inactivo.";
    case "HABIT_NOT_FOUND": return "El hábito ya no existe. Recarga el catálogo.";
    case "INVALID_HABIT_CADENCE": return "La cadencia no es válida.";
    case "BACKEND_UNAVAILABLE": return "No se pudo confirmar el resultado. Recarga el catálogo antes de continuar.";
    default: return "No se pudo completar la operación. Revisa el catálogo antes de volver a intentarlo.";
  }
}

export default function HabitsPage({ onSavingChange }) {
  const [habits, setHabits] = useState(null);
  const [status, setStatus] = useState("loading");
  const [problem, setProblem] = useState(null);
  const [saving, setSaving] = useState(null);
  const writePending = useRef(false);

  useEffect(() => {
    let ignore = false;
    loadHabits().then((data) => {
      if (!ignore) { setHabits(data); setStatus("ready"); }
    }).catch(() => {
      if (!ignore) { setStatus("error"); setProblem("No se pudieron cargar los hábitos."); }
    });
    return () => { ignore = true; };
  }, []);

  async function reload() {
    setStatus("loading");
    try {
      setHabits(await loadHabits());
      setProblem(null);
      setStatus("ready");
    } catch {
      setStatus("error");
      setProblem("No se pudieron cargar los hábitos. Reintenta la carga.");
    }
  }

  async function save(key, operation) {
    if (writePending.current || status !== "ready") return false;
    writePending.current = true;
    setSaving(key);
    onSavingChange?.(true);
    try {
      try {
        await operation();
      } catch (error) {
        if (error?.code === "BACKEND_UNAVAILABLE" || error?.code === "HABIT_NOT_FOUND" || error?.code === "HABIT_CATALOG_CHANGED" || error?.code === "INVALID_HABIT_ORDER" || !error?.status || error.status >= 500) {
          setStatus("stale");
          setProblem(errorMessage(error));
        }
        throw error;
      }
      try {
        setHabits(await loadHabits());
        setProblem(null);
      } catch {
        setStatus("stale");
        setProblem("El cambio se guardó, pero no se pudo recargar el catálogo. Recarga antes de continuar.");
      }
      return true;
    } finally {
      writePending.current = false;
      setSaving(null);
      onSavingChange?.(false);
    }
  }

  function moveHabit(index, direction) {
    const reordered = [...habits];
    const destination = index + direction;
    [reordered[index], reordered[destination]] = [reordered[destination], reordered[index]];
    return save(reordered[destination].habitId,
      () => reorderHabits(reordered.map((habit) => habit.habitId)));
  }

  const blocked = status !== "ready" || saving !== null;
  return (
    <main className="app-shell">
      <header className="page-header habits-header">
        <div><p className="eyebrow">Gestión de hábitos</p><h1>Hábitos</h1>
          <p className="page-intro">Crea, renombra y activa tus hábitos. Desactivarlos conserva su historial.</p></div>
      </header>
      {status === "loading" && <p className="context-message" role="status">Cargando hábitos…</p>}
      {problem && <div className="context-message context-message--warning" role="alert">
        <p>{problem}</p><button type="button" onClick={reload} disabled={status === "loading"}>Recargar catálogo</button>
      </div>}
      <section className="habit-card create-habit" aria-label="Crear hábito">
        <h2>Nuevo hábito</h2>
        <HabitForm blocked={blocked} saving={saving === "create"}
          onSave={(name, cadence, scoringGuide) => save("create", () => createHabit({ habitName: name, cadence, scoringGuide }))} />
      </section>
      {habits?.length === 0 && <p className="empty-state">Todavía no hay hábitos. Crea el primero.</p>}
      <section className="habit-grid" aria-label="Catálogo de hábitos">
        {habits?.map((habit, index) => <HabitCard key={habit.habitId} habit={habit} blocked={blocked}
          first={index === 0} last={index === habits.length - 1}
          onMove={(direction) => moveHabit(index, direction)}
          saving={saving === habit.habitId}
          onSetScoringGuide={(guide) => save(habit.habitId, () => setHabitScoringGuide(habit.habitId, guide))}
          onRename={(name) => save(habit.habitId, () => renameHabit(habit.habitId, name))}
          onSetActive={() => save(habit.habitId, () => setHabitActive(habit.habitId, !habit.active))} />)}
      </section>
    </main>
  );
}

function HabitForm({ habit, blocked, saving, onSave, onCancel }) {
  const [name, setName] = useState(habit?.habitName ?? "");
  const [scoringGuide, setScoringGuide] = useState("");
  const [cadence, setCadence] = useState("DAILY");
  const [feedback, setFeedback] = useState(null);
  async function submit(event) {
    event.preventDefault();
    if (blocked) return;
    setFeedback(null);
    try {
      if (await onSave(name, cadence, scoringGuide)) {
        if (habit) onCancel();
        else { setName(""); setScoringGuide(""); setFeedback({ message: "Hábito creado.", error: false }); }
      }
    } catch (error) {
      setFeedback({ message: errorMessage(error), error: true });
    }
  }
  return <form className="habit-management-form" onSubmit={submit}>
    <label className="management-field">Nombre
      <input value={name} onChange={(event) => setName(event.target.value)} disabled={blocked} required />
    </label>
    {!habit && <label className="management-field">Cadencia
      <select value={cadence} onChange={(event) => setCadence(event.target.value)} disabled={blocked}>
        <option value="DAILY">Diaria</option><option value="WEEKLY">Semanal</option>
      </select>
    </label>}
    {!habit && <label className="management-field">Guía de puntuación (opcional)
      <textarea rows={4} value={scoringGuide} onChange={(event) => setScoringGuide(event.target.value)} disabled={blocked} />
    </label>}
    <div className="habit-actions">
      <button className="save-button" disabled={blocked} type="submit">{saving ? "Guardando…" : habit ? "Guardar nombre" : "Crear hábito"}</button>
      {habit && <button type="button" onClick={onCancel} disabled={saving}>Cancelar</button>}
    </div>
    {feedback && <p className={`entry-feedback${feedback.error ? " entry-feedback--error" : ""}`} role={feedback.error ? "alert" : "status"}>{feedback.message}</p>}
  </form>;
}

function HabitCard({ habit, blocked, saving, onRename, onSetActive, onSetScoringGuide, first, last, onMove }) {
  const [editing, setEditing] = useState(false);
  const [editingGuide, setEditingGuide] = useState(false);
  const [feedback, setFeedback] = useState(null);
  async function changeActive() {
    if (blocked) return;
    setFeedback(null);
    try {
      if (await onSetActive()) setFeedback({ message: habit.active ? "Hábito desactivado." : "Hábito reactivado.", error: false });
    } catch (error) { setFeedback({ message: errorMessage(error), error: true }); }
  }
  async function move(direction) {
    if (blocked) return;
    setFeedback(null);
    try {
      if (await onMove(direction)) setFeedback({ message: "Orden guardado.", error: false });
    } catch (error) { setFeedback({ message: errorMessage(error), error: true }); }
  }
  return <article className="habit-card" aria-label={habit.habitName}>
    <div className="habit-card__header"><h2>{habit.habitName}</h2>
      <span className={habit.active ? "entry-badge" : "entry-badge entry-badge--empty"}>{habit.active ? "Activo" : "Inactivo"}</span></div>
    <p>{habit.cadence === "DAILY" ? "Diaria" : "Semanal"}</p>
    {habit.scoringGuide && <p className="scoring-guide">{habit.scoringGuide}</p>}
    {editingGuide ? <ScoringGuideForm habit={habit} blocked={blocked} saving={saving} onSave={onSetScoringGuide} onCancel={() => setEditingGuide(false)} /> : editing ? <HabitForm habit={habit} blocked={blocked} saving={saving} onSave={onRename} onCancel={() => setEditing(false)} /> :
      <div className="habit-actions">
        <button type="button" disabled={blocked} onClick={() => { setEditing(true); setFeedback(null); }}>Renombrar</button>
        <button type="button" disabled={blocked} onClick={() => { setEditingGuide(true); setFeedback(null); }}>Editar guía</button>
        <button type="button" disabled={blocked} onClick={changeActive}>{saving ? "Guardando…" : habit.active ? "Desactivar" : "Reactivar"}</button>
      </div>}
    <div className="habit-actions habit-order-actions" aria-label="Orden del hábito">
      <button type="button" disabled={blocked || editing || editingGuide || first} onClick={() => move(-1)}>Subir</button>
      <button type="button" disabled={blocked || editing || editingGuide || last} onClick={() => move(1)}>Bajar</button>
    </div>
    {feedback && <p className={`entry-feedback${feedback.error ? " entry-feedback--error" : ""}`} role={feedback.error ? "alert" : "status"}>{feedback.message}</p>}
  </article>;
}

function ScoringGuideForm({ habit, blocked, saving, onSave, onCancel }) {
  const [guide, setGuide] = useState(habit.scoringGuide ?? "");
  const [problem, setProblem] = useState(null);
  async function submit(event) {
    event.preventDefault();
    if (blocked) return;
    setProblem(null);
    try {
      if (await onSave(guide)) onCancel();
    } catch (error) { setProblem(errorMessage(error)); }
  }
  return <form className="habit-management-form" onSubmit={submit}>
    <label className="management-field">Guía de puntuación
      <textarea rows={4} value={guide} onChange={(event) => setGuide(event.target.value)} disabled={blocked} />
    </label>
    <p>Texto de referencia para puntuar. Déjalo vacío para eliminar la guía.</p>
    <div className="habit-actions">
      <button className="save-button" type="submit" disabled={blocked}>{saving ? "Guardando…" : "Guardar guía"}</button>
      <button type="button" disabled={saving} onClick={onCancel}>Cancelar</button>
    </div>
    {problem && <p className="entry-feedback entry-feedback--error" role="alert">{problem}</p>}
  </form>;
}
