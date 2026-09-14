import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
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
    vi.useRealTimers();
    vi.unstubAllGlobals();
    vi.clearAllMocks();
    loadDailyEntryContext.mockResolvedValue(contextWithExistingZero());
    createDailyEntry.mockResolvedValue(undefined);
    updateDailyEntry.mockResolvedValue(undefined);
  });

  it("loads the selected date and renders the stored context", async () => {
    render(<DailyEntryPage />);

    expect(
      screen.getByRole("img", { name: "Ramón, la mascota de Habit Tracker" }),
    ).toBeVisible();
    expect(await screen.findByRole("heading", { name: "Sleep" })).toBeVisible();
    expect(screen.getByLabelText("Valor almacenado")).toHaveTextContent(
      "Actual0Tired",
    );
    expect(loadDailyEntryContext).toHaveBeenCalledWith(
      screen.getByLabelText("Fecha").value,
    );
  });

  it("shows loading while the selected context is pending", async () => {
    const contextRequest = deferred();
    loadDailyEntryContext.mockReturnValueOnce(contextRequest.promise);

    render(<DailyEntryPage />);

    expect(screen.getByRole("status")).toHaveTextContent("Cargando hábitos");

    await act(async () => {
      contextRequest.resolve(contextWithExistingZero());
    });

    expect(await screen.findByRole("heading", { name: "Sleep" })).toBeVisible();
  });

  it("shows a load error and retries the same date", async () => {
    loadDailyEntryContext.mockRejectedValueOnce({
      code: "BACKEND_UNAVAILABLE",
    });
    render(<DailyEntryPage />);

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "No se puede conectar con el servidor",
    );

    await userEvent.click(screen.getByRole("button", { name: "Reintentar" }));

    expect(await screen.findByRole("heading", { name: "Sleep" })).toBeVisible();
    expect(loadDailyEntryContext).toHaveBeenCalledTimes(2);
  });

  it("lets Ramón introduce an empty habit list", async () => {
    loadDailyEntryContext.mockResolvedValueOnce({
      date: "2026-09-03",
      habits: [],
    });
    render(<DailyEntryPage />);

    expect(
      await screen.findByText("Ramón no ha encontrado hábitos activos."),
    ).toBeVisible();
  });

  it("keeps Ramón in view after his header image scrolls away", () => {
    let notifyIntersection;
    const disconnect = vi.fn();
    const observe = vi.fn();

    class IntersectionObserverMock {
      constructor(callback) {
        notifyIntersection = callback;
      }

      observe(...args) {
        observe(...args);
      }

      disconnect(...args) {
        disconnect(...args);
      }
    }

    vi.stubGlobal("IntersectionObserver", IntersectionObserverMock);

    const { container, unmount } = render(<DailyEntryPage />);
    const ramonCompanion = container.querySelector(".ramon-companion");

    expect(observe).toHaveBeenCalledOnce();
    expect(ramonCompanion).not.toHaveClass("ramon-companion--visible");

    act(() => notifyIntersection([{ isIntersecting: false }]));

    expect(ramonCompanion).toHaveClass("ramon-companion--visible");

    act(() => notifyIntersection([{ isIntersecting: true }]));

    expect(ramonCompanion).not.toHaveClass("ramon-companion--visible");
    expect(container.querySelector(".ramon-companion")).toBe(ramonCompanion);

    unmount();
    expect(disconnect).toHaveBeenCalledOnce();
  });

  it("shows Ramón eating an apple after seven clicks", () => {
    vi.useFakeTimers();
    render(<DailyEntryPage />);
    const ramon = screen.getByRole("button", {
      name: "Ramón, la mascota de Habit Tracker",
    });

    for (let click = 1; click < 7; click += 1) {
      fireEvent.click(ramon);
    }

    expect(
      screen.queryByRole("status", { name: "Ramón se come una manzana" }),
    ).not.toBeInTheDocument();

    fireEvent.click(ramon);

    expect(
      screen.getByRole("status", { name: "Ramón se come una manzana" }),
    ).toBeVisible();

    act(() => vi.advanceTimersByTime(1600));

    expect(
      screen.queryByRole("status", { name: "Ramón se come una manzana" }),
    ).not.toBeInTheDocument();
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

  it("blocks date changes and duplicate writes while saving", async () => {
    const createRequest = deferred();
    loadDailyEntryContext
      .mockResolvedValueOnce(contextWithMissingEntry())
      .mockResolvedValueOnce(contextWithExistingZero());
    createDailyEntry.mockReturnValueOnce(createRequest.promise);
    const user = userEvent.setup();
    render(<DailyEntryPage />);

    await screen.findByRole("heading", { name: "Sleep" });
    await user.click(screen.getByRole("radio", { name: "2" }));
    await user.click(screen.getByRole("button", { name: "Guardar entrada" }));

    expect(screen.getByRole("button", { name: "Guardando…" })).toBeDisabled();
    expect(screen.getByLabelText("Fecha")).toBeDisabled();
    expect(screen.getByRole("radio", { name: "2" })).toBeDisabled();
    expect(createDailyEntry).toHaveBeenCalledTimes(1);

    await act(async () => {
      createRequest.resolve();
    });

    expect(await screen.findByText("Entrada guardada.")).toBeVisible();
    expect(screen.getByLabelText("Fecha")).toBeEnabled();
  });

  it("marks the context stale and blocks writes when refresh fails after saving", async () => {
    const user = userEvent.setup();
    loadDailyEntryContext
      .mockResolvedValueOnce(contextWithMissingEntry())
      .mockRejectedValueOnce({ code: "BACKEND_UNAVAILABLE" })
      .mockResolvedValueOnce(contextWithExistingZero());
    render(<DailyEntryPage />);

    await screen.findByRole("heading", { name: "Sleep" });
    await user.click(screen.getByRole("radio", { name: "3" }));
    await user.click(screen.getByRole("button", { name: "Guardar entrada" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "El contexto está desactualizado",
    );
    expect(
      screen.getByText(
        "La entrada se guardó, pero el contexto quedó desactualizado.",
      ),
    ).toBeVisible();
    expect(screen.getByRole("button", { name: "Guardar entrada" })).toBeDisabled();
    expect(createDailyEntry).toHaveBeenCalledTimes(1);

    await user.click(
      screen.getByRole("button", { name: "Reintentar carga" }),
    );

    expect(
      await screen.findByRole("button", { name: "Guardar corrección" }),
    ).toBeEnabled();
    expect(screen.queryByText("El contexto está desactualizado.")).not.toBeInTheDocument();
    expect(
      screen.queryByText(
        "La entrada se guardó, pero el contexto quedó desactualizado.",
      ),
    ).not.toBeInTheDocument();
  });

  it("treats a connection failure during a write as uncertain and blocks further writes", async () => {
    const user = userEvent.setup();
    loadDailyEntryContext
      .mockResolvedValueOnce(contextWithMissingEntry())
      .mockResolvedValueOnce(contextWithExistingZero());
    createDailyEntry.mockRejectedValueOnce({
      code: "BACKEND_UNAVAILABLE",
    });
    render(<DailyEntryPage />);

    await screen.findByRole("heading", { name: "Sleep" });
    await user.click(screen.getByRole("radio", { name: "2" }));
    await user.click(screen.getByRole("button", { name: "Guardar entrada" }));

    expect(
      await screen.findByText(/no se puede saber si la entrada cambió/),
    ).toBeVisible();
    expect(
      screen.getByText(
        "No se pudo confirmar si la entrada se guardó. Recarga el contexto antes de continuar.",
      ),
    ).toBeVisible();
    expect(screen.getByRole("button", { name: "Guardar entrada" })).toBeDisabled();
    expect(createDailyEntry).toHaveBeenCalledTimes(1);

    await user.click(
      screen.getByRole("button", { name: "Reintentar carga" }),
    );

    expect(
      await screen.findByRole("button", { name: "Guardar corrección" }),
    ).toBeEnabled();
    expect(
      screen.queryByText(
        "No se pudo confirmar si la entrada se guardó. Recarga el contexto antes de continuar.",
      ),
    ).not.toBeInTheDocument();
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

function deferred() {
  let resolve;
  let reject;
  const promise = new Promise((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });

  return { promise, resolve, reject };
}
