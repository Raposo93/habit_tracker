import { afterEach, expect, it, vi } from "vitest";
import { createHabit, loadHabits, renameHabit, reorderHabits, setHabitActive } from "./habits.js";
afterEach(() => vi.unstubAllGlobals());

it("loads the full catalog", async () => {
  const habits = [{ habitId: "id", active: false }];
  const fetch = vi.fn().mockResolvedValue({ ok: true, json: async () => ({ habits }) });
  vi.stubGlobal("fetch", fetch);
  expect(await loadHabits()).toEqual(habits);
  expect(fetch).toHaveBeenCalledWith("/api/habits");
});

it("writes exact payloads by encoded identity without reading 204 JSON", async () => {
  const json = vi.fn().mockRejectedValue(new Error("No content"));
  const fetch = vi.fn().mockResolvedValue({ ok: true, status: 204, json });
  vi.stubGlobal("fetch", fetch);
  await createHabit({ habitName: "Walk", cadence: "DAILY" });
  await renameHabit("a/b", "Rest");
  await setHabitActive("a/b", false);
  expect(fetch.mock.calls.map(([url, options]) => [url, options.method, JSON.parse(options.body)])).toEqual([
    ["/api/habits", "POST", { habitName: "Walk", cadence: "DAILY" }],
    ["/api/habits/a%2Fb/name", "PUT", { habitName: "Rest" }],
    ["/api/habits/a%2Fb/active", "PUT", { active: false }],
  ]);
  expect(json).not.toHaveBeenCalled();
});

it("preserves API error code and status", async () => {
  vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 409, json: async () => ({ code: "HABIT_NAME_ALREADY_EXISTS", message: "Duplicate" }) }));
  await expect(renameHabit("id", "Rest")).rejects.toMatchObject({ code: "HABIT_NAME_ALREADY_EXISTS", status: 409 });
});

it("handles unavailable backends and invalid catalogs", async () => {
  const fetch = vi.fn().mockRejectedValueOnce(new Error()).mockResolvedValueOnce({ ok: true, json: async () => ({}) });
  vi.stubGlobal("fetch", fetch);
  await expect(loadHabits()).rejects.toMatchObject({ code: "BACKEND_UNAVAILABLE" });
  await expect(loadHabits()).rejects.toMatchObject({ code: "INVALID_RESPONSE" });
});

it("sends the complete habit order and accepts no-content responses", async () => {
  const fetch = vi.fn().mockResolvedValue({ ok: true, status: 204 });
  vi.stubGlobal("fetch", fetch);
  await reorderHabits(["inactive", "active"]);
  expect(fetch).toHaveBeenCalledWith("/api/habits/order", {
    method: "PUT", headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ habitIds: ["inactive", "active"] }),
  });
});
