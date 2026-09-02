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

  useEffect(() => {
    let ignoreResult = false;
    setContext(null);

    loadDailyEntryContext(selectedDate)
      .then((loadedContext) => {
        if (!ignoreResult) {
          setContext(loadedContext);
        }
      })
      .catch(() => {
        if (!ignoreResult) {
          window.alert("No se pudieron cargar los hábitos.");
        }
      });

    return () => {
      ignoreResult = true;
    };
  }, [selectedDate]);

  async function reloadContext() {
    const loadedContext = await loadDailyEntryContext(selectedDate);
    setContext(loadedContext);
  }

  async function createEntry(habitId, entry) {
    await createDailyEntry(selectedDate, habitId, entry);
    await reloadContext();
  }

  async function updateEntry(habitId, entry) {
    await updateDailyEntry(selectedDate, habitId, entry);
    await reloadContext();
  }

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
          />
        </label>
      </section>

      <section className="habit-section" aria-label="Hábitos activos">
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
            />
          ))}
        </div>
      </section>
    </main>
  );
}
