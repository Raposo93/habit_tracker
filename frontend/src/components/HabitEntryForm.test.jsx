import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import HabitEntryForm from "./HabitEntryForm.jsx";

describe("HabitEntryForm", () => {
  it("creates a missing entry by habit id and preserves score zero", async () => {
    const user = userEvent.setup();
    const onCreate = vi.fn().mockResolvedValue(undefined);
    const onUpdate = vi.fn();
    render(
      <HabitEntryForm
        habit={missingEntryHabit()}
        onCreate={onCreate}
        onUpdate={onUpdate}
      />,
    );

    await user.click(screen.getByRole("radio", { name: "0" }));
    await user.type(screen.getByRole("textbox", { name: "Nota" }), "Tired");
    await user.click(screen.getByRole("button", { name: "Guardar entrada" }));

    expect(onCreate).toHaveBeenCalledWith("sleep", {
      score: 0,
      note: "Tired",
    });
    expect(onUpdate).not.toHaveBeenCalled();
  });

  it.each([
    ["0,5", 0.5],
    ["1,5", 1.5],
    ["2,5", 2.5],
  ])("creates an entry with the half score %s", async (label, value) => {
    const user = userEvent.setup();
    const onCreate = vi.fn().mockResolvedValue(undefined);
    render(
      <HabitEntryForm
        habit={missingEntryHabit()}
        onCreate={onCreate}
        onUpdate={vi.fn()}
      />,
    );

    await user.click(screen.getByRole("radio", { name: label }));

    expect(screen.getByLabelText("Score seleccionado")).toHaveTextContent(label);

    await user.click(screen.getByRole("button", { name: "Guardar entrada" }));

    expect(onCreate).toHaveBeenCalledWith("sleep", {
      score: value,
      note: "",
    });
  });

  it("shows stored and draft values before updating an existing entry", async () => {
    const user = userEvent.setup();
    const confirm = vi.spyOn(window, "confirm").mockReturnValue(true);
    const onCreate = vi.fn();
    const onUpdate = vi.fn().mockResolvedValue(undefined);
    render(
      <HabitEntryForm
        habit={existingEntryHabit()}
        onCreate={onCreate}
        onUpdate={onUpdate}
      />,
    );

    expect(screen.getByLabelText("Valor almacenado")).toHaveTextContent(
      "Actual2Regular",
    );
    expect(
      screen.queryByRole("button", { name: "Ver nota completa" }),
    ).not.toBeInTheDocument();

    await user.click(screen.getByRole("radio", { name: "2,5" }));
    const note = screen.getByRole("textbox", { name: "Nota" });
    await user.clear(note);
    await user.type(note, "Much better");
    await user.click(
      screen.getByRole("button", { name: "Guardar corrección" }),
    );

    expect(confirm).toHaveBeenCalledWith(
      expect.stringContaining("Actual: 2 · Regular"),
    );
    expect(confirm).toHaveBeenCalledWith(
      expect.stringContaining("Nuevo: 2.5 · Much better"),
    );
    expect(onUpdate).toHaveBeenCalledWith("sleep", {
      score: 2.5,
      note: "Much better",
    });
    expect(onCreate).not.toHaveBeenCalled();
  });

  it("collapses a long stored note and lets the user expand it", async () => {
    const user = userEvent.setup();
    const longNote =
      "Una nota suficientemente larga para ocupar muchas líneas en la tarjeta y desplazar el resto de hábitos fuera de la vista. ".repeat(
        3,
      );
    render(
      <HabitEntryForm
        habit={{
          ...existingEntryHabit(),
          entry: { score: 2, note: longNote },
        }}
        onCreate={vi.fn()}
        onUpdate={vi.fn()}
      />,
    );

    const storedNote = screen
      .getByLabelText("Valor almacenado")
      .querySelector(".stored-entry__note");
    const expandButton = screen.getByRole("button", {
      name: "Ver nota completa",
    });

    expect(storedNote).toHaveClass("stored-entry__note--collapsed");
    expect(expandButton).toHaveAttribute("aria-expanded", "false");
    expect(expandButton).toHaveAttribute("aria-controls", storedNote.id);

    await user.click(expandButton);

    expect(storedNote).not.toHaveClass("stored-entry__note--collapsed");
    expect(
      screen.getByRole("button", { name: "Ocultar nota" }),
    ).toHaveAttribute("aria-expanded", "true");
  });

  it("does not update when correction is cancelled", async () => {
    const user = userEvent.setup();
    vi.spyOn(window, "confirm").mockReturnValue(false);
    const onCreate = vi.fn();
    const onUpdate = vi.fn();
    render(
      <HabitEntryForm
        habit={existingEntryHabit()}
        onCreate={onCreate}
        onUpdate={onUpdate}
      />,
    );

    await user.click(screen.getByRole("radio", { name: "3" }));
    await user.click(
      screen.getByRole("button", { name: "Guardar corrección" }),
    );

    expect(onUpdate).not.toHaveBeenCalled();
    expect(onCreate).not.toHaveBeenCalled();
  });
});

function missingEntryHabit() {
  return {
    habitId: "sleep",
    habitName: "Sleep",
    entry: null,
  };
}

function existingEntryHabit() {
  return {
    habitId: "sleep",
    habitName: "Sleep",
    entry: {
      score: 2,
      note: "Regular",
    },
  };
}
