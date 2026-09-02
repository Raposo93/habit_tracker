import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import {
  createDailyEntry,
  loadDailyEntryContext,
  updateDailyEntry,
} from "../api/dailyEntries.js";
import DailyEntryPage from "./DailyEntryPage.jsx";

vi.mock("../api/dailyEntries.js", () => ({
  createDailyEntry: vi.fn(),
  loadDailyEntryContext: vi.fn(),
  updateDailyEntry: vi.fn(),
}));

describe("DailyEntryPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    loadDailyEntryContext.mockResolvedValue(contextWithExistingZero());
    createDailyEntry.mockResolvedValue(undefined);
    updateDailyEntry.mockResolvedValue(undefined);
  });

  it("loads the selected date and renders the stored context", async () => {
    render(<DailyEntryPage />);

    expect(await screen.findByRole("heading", { name: "Sleep" })).toBeVisible();
    expect(screen.getByLabelText("Valor almacenado")).toHaveTextContent(
      "Actual0Tired",
    );
    expect(loadDailyEntryContext).toHaveBeenCalledWith(
      screen.getByLabelText("Fecha").value,
    );
  });

  it("loads a retrospective date without converting it", async () => {
    const user = userEvent.setup();
    render(<DailyEntryPage />);
    await screen.findByRole("heading", { name: "Sleep" });

    const date = screen.getByLabelText("Fecha");
    await user.clear(date);
    await user.type(date, "2026-08-15");

    await waitFor(() => {
      expect(loadDailyEntryContext).toHaveBeenCalledWith("2026-08-15");
    });
  });

  it("reloads the selected context after creating an entry", async () => {
    const user = userEvent.setup();
    loadDailyEntryContext
      .mockResolvedValueOnce(contextWithMissingEntry())
      .mockResolvedValueOnce(contextWithExistingZero());
    render(<DailyEntryPage />);

    await screen.findByRole("heading", { name: "Sleep" });
    const selectedDate = screen.getByLabelText("Fecha").value;
    await user.click(screen.getByRole("radio", { name: "0" }));
    await user.click(screen.getByRole("button", { name: "Guardar entrada" }));

    await waitFor(() => {
      expect(createDailyEntry).toHaveBeenCalledWith(selectedDate, "sleep", {
        score: 0,
        note: "",
      });
      expect(loadDailyEntryContext).toHaveBeenCalledTimes(2);
    });
    expect(await screen.findByText("Registrado")).toBeVisible();
  });

  it("updates an existing entry by habit id and reloads its date", async () => {
    const user = userEvent.setup();
    vi.spyOn(window, "confirm").mockReturnValue(true);
    render(<DailyEntryPage />);

    await screen.findByRole("heading", { name: "Sleep" });
    const selectedDate = screen.getByLabelText("Fecha").value;
    await user.click(screen.getByRole("radio", { name: "3" }));
    await user.click(
      screen.getByRole("button", { name: "Guardar corrección" }),
    );

    await waitFor(() => {
      expect(updateDailyEntry).toHaveBeenCalledWith(selectedDate, "sleep", {
        score: 3,
        note: "Tired",
      });
      expect(loadDailyEntryContext).toHaveBeenCalledTimes(2);
    });
  });
});

function contextWithMissingEntry() {
  return {
    date: "2026-09-03",
    habits: [
      {
        habitId: "sleep",
        habitName: "Sleep",
        entry: null,
      },
    ],
  };
}

function contextWithExistingZero() {
  return {
    date: "2026-09-03",
    habits: [
      {
        habitId: "sleep",
        habitName: "Sleep",
        entry: {
          score: 0,
          note: "Tired",
        },
      },
    ],
  };
}
