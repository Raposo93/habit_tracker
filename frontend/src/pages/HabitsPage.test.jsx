import { act, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, expect, it, vi } from "vitest";
import { createHabit, loadHabits, renameHabit, reorderHabits, setHabitActive } from "../api/habits.js";
import HabitsPage from "./HabitsPage.jsx";

vi.mock("../api/habits.js", () => ({ createHabit: vi.fn(), loadHabits: vi.fn(), renameHabit: vi.fn(), reorderHabits: vi.fn(), setHabitActive: vi.fn() }));
const sleep = { habitId: "stable-sleep", habitName: "Sleep", cadence: "DAILY", active: true };
const review = { habitId: "stable-review", habitName: "Review", cadence: "WEEKLY", active: false };
function deferred() { let resolve; const promise = new Promise((done) => { resolve = done; }); return { promise, resolve }; }
async function ready() { render(<HabitsPage />); await screen.findByRole("heading", { name: "Sleep" }); }
const creator = () => within(screen.getByRole("region", { name: "Crear hábito" }));
const card = (name) => within(screen.getByRole("article", { name }));

beforeEach(() => {
  vi.resetAllMocks();
  loadHabits.mockResolvedValue([sleep, review]);
  createHabit.mockResolvedValue(undefined);
  renameHabit.mockResolvedValue(undefined);
  setHabitActive.mockResolvedValue(undefined);
  reorderHabits.mockResolvedValue(undefined);
});

it("shows loading, active and inactive habits with their cadence", async () => {
  const pending = deferred(); loadHabits.mockReturnValueOnce(pending.promise);
  render(<HabitsPage />);
  expect(screen.getByRole("status")).toHaveTextContent("Cargando hábitos");
  expect(creator().getByRole("button", { name: "Crear hábito" })).toBeDisabled();
  await act(async () => pending.resolve([sleep, review]));
  expect(card("Sleep").getByText("Activo")).toBeVisible();
  expect(card("Review").getByText("Inactivo")).toBeVisible();
  expect(card("Review").getByText("Semanal")).toBeVisible();
});

it("shows an empty catalog", async () => {
  loadHabits.mockResolvedValue([]); render(<HabitsPage />);
  expect(await screen.findByText("Todavía no hay hábitos. Crea el primero.")).toBeVisible();
});

it("retries a failed initial load", async () => {
  loadHabits.mockRejectedValueOnce(new Error()); render(<HabitsPage />);
  await screen.findByRole("alert");
  await userEvent.click(screen.getByRole("button", { name: "Recargar catálogo" }));
  expect(await screen.findByRole("heading", { name: "Sleep" })).toBeVisible();
});

it("creates a weekly habit, refreshes from the server and resets its name", async () => {
  await ready();
  const newHabit = { habitId: "generated", habitName: "Walk", cadence: "WEEKLY", active: true };
  loadHabits.mockResolvedValue([sleep, review, newHabit]);
  await userEvent.type(creator().getByLabelText("Nombre"), "Walk");
  await userEvent.selectOptions(creator().getByLabelText("Cadencia"), "WEEKLY");
  await userEvent.click(creator().getByRole("button", { name: "Crear hábito" }));
  expect(createHabit).toHaveBeenCalledWith({ habitName: "Walk", cadence: "WEEKLY" });
  expect(await screen.findByRole("heading", { name: "Walk" })).toBeVisible();
  expect(creator().getByLabelText("Nombre")).toHaveValue("");
});

it("renames by stable identity and supports cancelling without a write", async () => {
  await ready();
  await userEvent.click(card("Sleep").getByRole("button", { name: "Renombrar" }));
  await userEvent.clear(card("Sleep").getByLabelText("Nombre"));
  await userEvent.type(card("Sleep").getByLabelText("Nombre"), "Rest");
  await userEvent.click(card("Sleep").getByRole("button", { name: "Cancelar" }));
  expect(renameHabit).not.toHaveBeenCalled();
  await userEvent.click(card("Sleep").getByRole("button", { name: "Renombrar" }));
  expect(card("Sleep").getByLabelText("Nombre")).toHaveValue("Sleep");
  await userEvent.clear(card("Sleep").getByLabelText("Nombre"));
  await userEvent.type(card("Sleep").getByLabelText("Nombre"), "Rest");
  loadHabits.mockResolvedValue([{ ...sleep, habitName: "Rest" }, review]);
  await userEvent.click(card("Sleep").getByRole("button", { name: "Guardar nombre" }));
  expect(renameHabit).toHaveBeenCalledWith("stable-sleep", "Rest");
  expect(await screen.findByRole("heading", { name: "Rest" })).toBeVisible();
});

it("deactivates and reactivates using the desired state", async () => {
  await ready();
  loadHabits.mockResolvedValue([{ ...sleep, active: false }, review]);
  await userEvent.click(card("Sleep").getByRole("button", { name: "Desactivar" }));
  expect(setHabitActive).toHaveBeenLastCalledWith("stable-sleep", false);
  expect(card("Sleep").getByText("Inactivo")).toBeVisible();
  loadHabits.mockResolvedValue([sleep, review]);
  await userEvent.click(card("Sleep").getByRole("button", { name: "Reactivar" }));
  expect(setHabitActive).toHaveBeenLastCalledWith("stable-sleep", true);
  expect(card("Sleep").getByText("Activo")).toBeVisible();
});

it("preserves a rejected name and shows the backend duplicate error", async () => {
  await ready(); renameHabit.mockRejectedValue({ code: "HABIT_NAME_ALREADY_EXISTS", status: 409 });
  await userEvent.click(card("Sleep").getByRole("button", { name: "Renombrar" }));
  await userEvent.type(card("Sleep").getByLabelText("Nombre"), " duplicate");
  await userEvent.click(card("Sleep").getByRole("button", { name: "Guardar nombre" }));
  expect(card("Sleep").getByRole("alert")).toHaveTextContent("Ya existe un hábito");
  expect(card("Sleep").getByLabelText("Nombre")).toHaveValue("Sleep duplicate");
  expect(card("Sleep").getByRole("button", { name: "Guardar nombre" })).toBeEnabled();
});

it("blocks duplicate submissions and all other writes while saving", async () => {
  const pending = deferred(); createHabit.mockReturnValueOnce(pending.promise);
  const onSaving = vi.fn(); render(<HabitsPage onSavingChange={onSaving} />);
  await screen.findByRole("heading", { name: "Sleep" });
  await userEvent.type(creator().getByLabelText("Nombre"), "Walk");
  await userEvent.dblClick(creator().getByRole("button", { name: "Crear hábito" }));
  expect(createHabit).toHaveBeenCalledTimes(1);
  expect(card("Sleep").getByRole("button", { name: "Desactivar" })).toBeDisabled();
  expect(onSaving).toHaveBeenLastCalledWith(true);
  await act(async () => pending.resolve());
  expect(onSaving).toHaveBeenLastCalledWith(false);
});

it("distinguishes a saved write from a failed refresh and retries only the read", async () => {
  await ready(); loadHabits.mockRejectedValueOnce(new Error());
  await userEvent.click(card("Sleep").getByRole("button", { name: "Desactivar" }));
  expect(screen.getByRole("alert")).toHaveTextContent("El cambio se guardó");
  expect(card("Sleep").getByRole("button", { name: "Desactivar" })).toBeDisabled();
  await userEvent.click(screen.getByRole("button", { name: "Recargar catálogo" }));
  expect(setHabitActive).toHaveBeenCalledTimes(1);
  expect(card("Sleep").getByRole("button", { name: "Desactivar" })).toBeEnabled();
});

it("blocks writes after an uncertain result and retains the draft until reload", async () => {
  await ready(); createHabit.mockRejectedValueOnce({ code: "BACKEND_UNAVAILABLE" });
  await userEvent.type(creator().getByLabelText("Nombre"), "Walk");
  await userEvent.click(creator().getByRole("button", { name: "Crear hábito" }));
  expect(creator().getByRole("alert")).toHaveTextContent("No se pudo confirmar");
  expect(creator().getByLabelText("Nombre")).toHaveValue("Walk");
  expect(creator().getByRole("button", { name: "Crear hábito" })).toBeDisabled();
  await userEvent.click(screen.getByRole("button", { name: "Recargar catálogo" }));
  expect(creator().getByRole("button", { name: "Crear hábito" })).toBeEnabled();
});

it("moves habits including inactive ones and displays the confirmed order", async () => {
  await ready();
  expect(card("Sleep").getByRole("button", { name: "Subir" })).toBeDisabled();
  expect(card("Review").getByRole("button", { name: "Bajar" })).toBeDisabled();
  loadHabits.mockResolvedValue([review, sleep]);
  await userEvent.click(card("Review").getByRole("button", { name: "Subir" }));
  expect(reorderHabits).toHaveBeenCalledWith(["stable-review", "stable-sleep"]);
  expect(screen.getAllByRole("article").map((item) => item.getAttribute("aria-label"))).toEqual(["Review", "Sleep"]);
  expect(card("Review").getByRole("button", { name: "Subir" })).toBeDisabled();
  loadHabits.mockResolvedValue([sleep, review]);
  await userEvent.click(card("Review").getByRole("button", { name: "Bajar" }));
  expect(reorderHabits).toHaveBeenLastCalledWith(["stable-sleep", "stable-review"]);
});

it("blocks repeated moves until the write and reload finish", async () => {
  await ready();
  const pending = deferred(); reorderHabits.mockReturnValueOnce(pending.promise);
  await userEvent.dblClick(card("Sleep").getByRole("button", { name: "Bajar" }));
  expect(reorderHabits).toHaveBeenCalledTimes(1);
  expect(card("Review").getByRole("button", { name: "Subir" })).toBeDisabled();
  expect(screen.getAllByRole("article")[0]).toHaveAttribute("aria-label", "Sleep");
  await act(async () => pending.resolve());
});

it("requires a catalog reload after a reorder conflict without changing the visible order", async () => {
  await ready(); reorderHabits.mockRejectedValueOnce({ code: "HABIT_CATALOG_CHANGED", status: 409 });
  await userEvent.click(card("Sleep").getByRole("button", { name: "Bajar" }));
  expect(card("Sleep").getByRole("alert")).toHaveTextContent("ha cambiado");
  expect(card("Sleep").getByRole("button", { name: "Bajar" })).toBeDisabled();
  expect(screen.getAllByRole("article")[0]).toHaveAttribute("aria-label", "Sleep");
  loadHabits.mockResolvedValue([review, sleep]);
  await userEvent.click(screen.getByRole("button", { name: "Recargar catálogo" }));
  expect(card("Sleep").getByRole("button", { name: "Subir" })).toBeEnabled();
});
