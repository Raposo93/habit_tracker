import { useEffect, useState } from "react";

import {
  createDailyEntry,
  loadDailyEntryContext,
  updateDailyEntry,
} from "../api/dailyEntries.js";
import HabitEntryForm from "../components/HabitEntryForm.jsx";

function todayAsApiDate() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

export default function DailyEntryPage() {
  const [selectedDate, setSelectedDate] = useState(todayAsApiDate);
  const [context, setContext] = useState(null);
  const [contextStatus, setContextStatus] = useState("loading");
  const [contextError, setContextError] = useState(null);
  const [staleReason, setStaleReason] = useState(null);
  const [savingHabitId, setSavingHabitId] = useState(null);

  useEffect(() => {
    let ignoreResult = false;
    setContext(null);
    setContextStatus("loading");
    setContextError(null);
    setStaleReason(null);

    loadDailyEntryContext(selectedDate)
      .then((loadedContext) => {
        if (!ignoreResult) {
          setContext(loadedContext);
          setContextStatus("ready");
        }
      })
      .catch((error) => {
        if (!ignoreResult) {
          setContextStatus("error");
          setContextError(error);
        }
      });

    return () => {
      ignoreResult = true;
    };
  }, [selectedDate]);

  async function retryContextLoad() {
    const statusAfterFailure = context === null ? "error" : "stale";
    setContextStatus("loading");
    setContextError(null);

    try {
      const loadedContext = await loadDailyEntryContext(selectedDate);
      setContext(loadedContext);
      setContextStatus("ready");
      setStaleReason(null);
    } catch (error) {
      setContextStatus(statusAfterFailure);
      setContextError(error);
    }
  }

  async function refreshContextAfterWrite() {
    try {
      const loadedContext = await loadDailyEntryContext(selectedDate);
      setContext(loadedContext);
      setContextStatus("ready");
      setContextError(null);
      setStaleReason(null);
      return true;
    } catch (error) {
      setContextStatus("stale");
      setContextError(error);
      setStaleReason("refresh-failed");
      return false;
    }
  }

  async function saveEntry(writeEntry, habitId, entry) {
    if (contextStatus !== "ready" || savingHabitId !== null) {
      throw new Error("Entry writes are currently blocked");
    }

    setSavingHabitId(habitId);

    try {
      try {
        await writeEntry(selectedDate, habitId, entry);
      } catch (error) {
        if (error?.code === "BACKEND_UNAVAILABLE") {
          setContextStatus("stale");
          setContextError(error);
          setStaleReason("write-uncertain");
        }

        throw error;
      }

      const refreshed = await refreshContextAfterWrite();
      return { refreshed };
    } finally {
      setSavingHabitId(null);
    }
  }

  function createEntry(habitId, entry) {
    return saveEntry(createDailyEntry, habitId, entry);
  }

  function updateEntry(habitId, entry) {
    return saveEntry(updateDailyEntry, habitId, entry);
  }

  const writeBlocked = contextStatus !== "ready" || savingHabitId !== null;
  const contextHasData = context !== null;

  return (
    <main className="app-shell">
      <header className="page-header">
        <p className="eyebrow">Seguimiento diario</p>
        <h1>Habit Tracker</h1>
        <p className="page-intro">
          Registra el día de hoy o corrige cualquier fecha anterior.
        </p>
      </header>

      <section className="date-panel" aria-labelledby="date-heading">
        <div>
          <h2 id="date-heading">Fecha de trabajo</h2>
          <p>Los cambios se guardarán exactamente en este día.</p>
        </div>
        <label className="date-field">
          <span>Fecha</span>
          <input
            type="date"
            value={selectedDate}
            onChange={(event) => setSelectedDate(event.target.value)}
            disabled={savingHabitId !== null}
            required
          />
        </label>
      </section>

      <section className="habit-section" aria-label="Hábitos activos">
        {contextStatus === "loading" && !contextHasData && (
          <p className="context-message" role="status">
            Cargando hábitos…
          </p>
        )}

        {contextStatus === "error" && (
          <ContextLoadProblem
            error={contextError}
            onRetry={retryContextLoad}
          />
        )}

        {contextStatus === "loading" && contextHasData && (
          <p className="context-message" role="status">
            Actualizando el contexto…
          </p>
        )}

        {contextStatus === "stale" && (
          <div className="context-message context-message--warning" role="alert">
            <div>
              <strong>El contexto está desactualizado.</strong>
              {staleReason === "write-uncertain" ? (
                <p>
                  Se perdió la conexión durante el guardado y no se puede saber
                  si la entrada cambió. Recarga el contexto antes de volver a
                  guardar.
                </p>
              ) : (
                <p>
                  La entrada se guardó, pero no se pudieron recargar los datos.
                  No puedes guardar más cambios hasta actualizar el contexto.
                </p>
              )}
            </div>
            <button type="button" onClick={retryContextLoad}>
              Reintentar carga
            </button>
          </div>
        )}

        {context?.habits.length === 0 && (
          <p className="empty-state">No hay hábitos activos.</p>
        )}

        <div className="habit-grid">
          {context?.habits.map((habit) => (
            <HabitEntryForm
              key={`${selectedDate}:${habit.habitId}`}
              habit={habit}
              onCreate={createEntry}
              onUpdate={updateEntry}
              isSaving={savingHabitId === habit.habitId}
              writeBlocked={writeBlocked}
              contextStatus={contextStatus}
            />
          ))}
        </div>
      </section>
    </main>
  );
}

function ContextLoadProblem({ error, onRetry }) {
  const message =
    error?.code === "BACKEND_UNAVAILABLE"
      ? "No se puede conectar con el servidor."
      : error?.code === "INVALID_DATE"
        ? "La fecha seleccionada no es válida."
        : "No se pudieron cargar los hábitos.";

  return (
    <div className="context-message context-message--error" role="alert">
      <p>{message}</p>
      <button type="button" onClick={onRetry}>
        Reintentar
      </button>
    </div>
  );
}
