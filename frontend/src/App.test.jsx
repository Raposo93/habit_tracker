import { act, fireEvent, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, expect, it, vi } from "vitest";
import App from "./App.jsx";
import { loadDailyEntryContext } from "./api/dailyEntries.js";
import { loadHabits, setHabitActive } from "./api/habits.js";
vi.mock("./api/dailyEntries.js", () => ({ loadDailyEntryContext: vi.fn(), createDailyEntry: vi.fn(), updateDailyEntry: vi.fn() }));
vi.mock("./api/habits.js", () => ({ loadHabits: vi.fn(), createHabit: vi.fn(), renameHabit: vi.fn(), setHabitActive: vi.fn() }));
const habit = { habitId: "sleep", habitName: "Sleep", cadence: "DAILY", active: true };
beforeEach(() => {
  vi.resetAllMocks();
  loadDailyEntryContext.mockImplementation(async (date) => ({ date, habits: [{ ...habit, entry: null }] }));
  loadHabits.mockResolvedValue([habit]);
});
it("preserves the date and reloads Daily Entry after habit management", async () => {
  render(<App />); await screen.findByRole("heading", { name: "Sleep" });
  fireEvent.change(screen.getByLabelText("Fecha"), { target: { value: "2026-08-10" } });
  await screen.findByRole("heading", { name: "Sleep" });
  await userEvent.click(screen.getByRole("button", { name: "Hábitos", exact: true }));
  await screen.findByRole("heading", { name: "Sleep" });
  loadDailyEntryContext.mockResolvedValue({ date: "2026-08-10", habits: [] });
  await userEvent.click(screen.getByRole("button", { name: "Registro diario" }));
  expect(await screen.findByText("Ramón no ha encontrado hábitos activos.")).toBeVisible();
  expect(screen.getByLabelText("Fecha")).toHaveValue("2026-08-10");
  expect(loadDailyEntryContext).toHaveBeenLastCalledWith("2026-08-10");
});
it("blocks navigation during management writes", async () => {
  let resolve; setHabitActive.mockReturnValue(new Promise((done) => { resolve = done; }));
  render(<App />); await screen.findByRole("heading", { name: "Sleep" });
  await userEvent.click(screen.getByRole("button", { name: "Hábitos", exact: true }));
  await screen.findByRole("heading", { name: "Sleep" });
  await userEvent.click(screen.getByRole("button", { name: "Desactivar" }));
  const nav = within(screen.getByRole("navigation"));
  expect(nav.getByRole("button", { name: "Registro diario" })).toBeDisabled();
  await act(async () => resolve());
  expect(nav.getByRole("button", { name: "Registro diario" })).toBeEnabled();
});
