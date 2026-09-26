import { useState } from "react";
import DailyEntryPage from "./pages/DailyEntryPage.jsx";
import HabitsPage from "./pages/HabitsPage.jsx";

export default function App() {
  const [page, setPage] = useState("daily");
  const [selectedDate, setSelectedDate] = useState(null);
  const [saving, setSaving] = useState(false);
  return <>
    <nav className="app-navigation" aria-label="Navegación principal">
      <button type="button" aria-current={page === "daily" ? "page" : undefined} disabled={saving}
        onClick={() => setPage("daily")}>Registro diario</button>
      <button type="button" aria-current={page === "habits" ? "page" : undefined} disabled={saving}
        onClick={() => setPage("habits")}>Hábitos</button>
    </nav>
    {page === "daily" ? <DailyEntryPage initialDate={selectedDate} onDateChange={setSelectedDate} onSavingChange={setSaving} /> :
      <HabitsPage onSavingChange={setSaving} />}
  </>;
}
