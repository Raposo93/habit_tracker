import { afterEach, describe, expect, it, vi } from "vitest";

import {
  createDailyEntry,
  loadDailyEntryContext,
  updateDailyEntry,
} from "./dailyEntries.js";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("daily entries API", () => {
  it("loads the context for the selected API date", async () => {
    const context = {
      date: "2026-09-03",
      habits: [],
    };
    const fetch = vi.fn().mockResolvedValue(successResponse(context));
    vi.stubGlobal("fetch", fetch);

    await expect(loadDailyEntryContext("2026-09-03")).resolves.toEqual(context);
    expect(fetch).toHaveBeenCalledWith(
      "/api/entries/context?date=2026-09-03",
    );
  });

  it("creates an entry by habit id", async () => {
    const fetch = vi.fn().mockResolvedValue(successResponse());
    vi.stubGlobal("fetch", fetch);

    await createDailyEntry("2026-09-03", "sleep", {
      score: 0,
      note: "Tired",
    });

    expect(fetch).toHaveBeenCalledWith("/api/entries/2026-09-03/sleep", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ score: 0, note: "Tired" }),
    });
  });

  it("updates an entry by habit id", async () => {
    const fetch = vi.fn().mockResolvedValue(successResponse());
    vi.stubGlobal("fetch", fetch);

    await updateDailyEntry("2026-09-03", "sleep", {
      score: 3,
      note: "Rested",
    });

    expect(fetch).toHaveBeenCalledWith("/api/entries/2026-09-03/sleep", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ score: 3, note: "Rested" }),
    });
  });
});

function successResponse(body) {
  return {
    ok: true,
    status: 200,
    json: vi.fn().mockResolvedValue(body),
  };
}
