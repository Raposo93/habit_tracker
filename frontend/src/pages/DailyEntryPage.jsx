import { useEffect, useRef, useState } from "react";

import {
  createDailyEntry,
  loadDailyEntryContext,
  updateDailyEntry,
} from "../api/dailyEntries.js";
import HabitEntryForm from "../components/HabitEntryForm.jsx";

const RAMON_EASTER_EGG_CLICK_COUNT = 7;
const RAMON_EASTER_EGG_DURATION_MS = 1600;

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
  const [ramonIsFollowing, setRamonIsFollowing] = useState(false);
  const [ramonEatingTarget, setRamonEatingTarget] = useState(null);
  const ramonHeaderRef = useRef(null);
  const ramonClickCount = useRef(0);

  useEffect(() => {
    const ramonHeader = ramonHeaderRef.current;

    if (ramonHeader === null || !("IntersectionObserver" in window)) {
      return undefined;
    }

    const observer = new IntersectionObserver(
      ([entry]) => setRamonIsFollowing(!entry.isIntersecting),
      { threshold: 0.25 },
    );

    observer.observe(ramonHeader);

    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    if (ramonEatingTarget === null) {
      return undefined;
    }

    const hideAnimation = window.setTimeout(
      () => setRamonEatingTarget(null),
      RAMON_EASTER_EGG_DURATION_MS,
    );

    return () => window.clearTimeout(hideAnimation);
  }, [ramonEatingTarget]);

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

  function greetRamon(target) {
    if (ramonEatingTarget !== null) {
      return;
    }

    ramonClickCount.current += 1;

    if (ramonClickCount.current === RAMON_EASTER_EGG_CLICK_COUNT) {
      ramonClickCount.current = 0;
      setRamonEatingTarget(target);
    }
  }

  const writeBlocked = contextStatus !== "ready" || savingHabitId !== null;
  const contextHasData = context !== null;

  return (
    <main className="app-shell">
      <header className="page-header">
        <div>
          <p className="eyebrow">Seguimiento diario</p>
          <h1>Habit Tracker</h1>
          <p className="page-intro">
            Registra el día de hoy o corrige cualquier fecha anterior.
          </p>
        </div>
        <button
          ref={ramonHeaderRef}
          className={`ramon-trigger ramon-trigger--header ${
            ramonEatingTarget === "header" ? "ramon-trigger--eating" : ""
          }`}
          type="button"
          onClick={() => greetRamon("header")}
        >
          <img
            className="page-header__mascot"
            src="/assets/ramon.png"
            alt="Ramón, la mascota de Habit Tracker"
          />
          {ramonEatingTarget === "header" && (
            <span
              className="ramon-easter-egg__sprite"
              onAnimationEnd={() => setRamonEatingTarget(null)}
              aria-hidden="true"
            />
          )}
        </button>
      </header>

      <button
        className={`ramon-trigger ramon-companion ${
          ramonIsFollowing ? "ramon-companion--visible" : ""
        } ${
          ramonEatingTarget === "companion" ? "ramon-trigger--eating" : ""
        }`}
        type="button"
        onClick={() => greetRamon("companion")}
        tabIndex="-1"
        aria-hidden="true"
      >
        <img
          className="ramon-companion__image"
          src="/assets/ramon.png"
          alt=""
        />
        {ramonEatingTarget === "companion" && (
          <span
            className="ramon-easter-egg__sprite"
            onAnimationEnd={() => setRamonEatingTarget(null)}
            aria-hidden="true"
          />
        )}
      </button>

      {ramonEatingTarget !== null && (
        <span
          className="ramon-easter-egg__status"
          role="status"
          aria-label="Ramón se come una manzana"
        />
      )}

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
          <p className="empty-state">
            Ramón no ha encontrado hábitos activos.
          </p>
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
  const detail =
    error?.code === "BACKEND_UNAVAILABLE"
      ? "No se puede conectar con el servidor."
      : error?.code === "INVALID_DATE"
        ? "La fecha seleccionada no es válida."
        : "No se pudieron cargar los hábitos.";

  return (
    <div className="context-message context-message--error" role="alert">
      <strong>Ramón no pudo cargar tus hábitos.</strong>
      <p>{detail}</p>
      <button type="button" onClick={onRetry}>
        Reintentar
      </button>
    </div>
  );
}
