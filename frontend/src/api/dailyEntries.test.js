import { afterEach, describe, expect, it, vi } from "vitest";

import {
  createDailyEntry,
  DailyEntryApiError,
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

  it("preserves the backend error code, message and status", async () => {
    const fetch = vi.fn().mockResolvedValue(
      errorResponse(400, {
        code: "INVALID_SCORE",
        message: "Score must be between 0 and 3",
      }),
    );
    vi.stubGlobal("fetch", fetch);

    const request = createDailyEntry("2026-09-03", "sleep", {
      score: 4,
      note: "",
    });

    await expect(request).rejects.toBeInstanceOf(DailyEntryApiError);
    await expect(request).rejects.toMatchObject({
      code: "INVALID_SCORE",
      message: "Score must be between 0 and 3",
      status: 400,
    });
  });

  it("distinguishes an unavailable backend from an HTTP error", async () => {
    const fetchFailure = new TypeError("Failed to fetch");
    const fetch = vi.fn().mockRejectedValue(fetchFailure);
    vi.stubGlobal("fetch", fetch);

    await expect(loadDailyEntryContext("2026-09-03")).rejects.toMatchObject({
      code: "BACKEND_UNAVAILABLE",
      status: null,
      cause: fetchFailure,
    });
  });

  it("uses a safe fallback when an HTTP error has no JSON body", async () => {
    const fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 500,
      json: vi.fn().mockRejectedValue(new SyntaxError("Unexpected end")),
    });
    vi.stubGlobal("fetch", fetch);

    await expect(updateDailyEntry("2026-09-03", "sleep", {})).rejects.toMatchObject(
      {
        code: "HTTP_ERROR",
        message: "Request failed with status 500",
        status: 500,
      },
    );
  });
});

function successResponse(body) {
  return {
    ok: true,
    status: 200,
    json: vi.fn().mockResolvedValue(body),
  };
}

function errorResponse(status, body) {
  return {
    ok: false,
    status,
    json: vi.fn().mockResolvedValue(body),
  };
}
