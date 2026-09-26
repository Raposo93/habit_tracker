import { act, render, screen } from "@testing-library/react";
import { afterEach, expect, it, vi } from "vitest";

import Ramon from "./Ramon.jsx";

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

it("uses the selected animation and completes one-shot reactions", () => {
  vi.useFakeTimers();
  const onComplete = vi.fn();
  const { rerender } = render(<Ramon animation="happy" label="Ramón" onComplete={onComplete} />);

  expect(screen.getByRole("img", { name: "Ramón" })).toHaveAttribute("data-animation", "happy");
  expect(screen.getByRole("img", { name: "Ramón" }).style.backgroundImage).toContain("spritesheet.png");

  act(() => vi.advanceTimersByTime(900));
  expect(onComplete).toHaveBeenCalledOnce();

  rerender(<Ramon animation="idle" label="Ramón" />);
  expect(screen.getByRole("img", { name: "Ramón" })).toHaveAttribute("data-animation", "idle");
});

it("keeps a static frame with reduced motion and still completes the reaction", () => {
  vi.useFakeTimers();
  vi.stubGlobal("matchMedia", () => ({
    matches: true,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
  }));
  const onComplete = vi.fn();
  render(<Ramon animation="apple" label="Ramón" onComplete={onComplete} />);
  const sprite = screen.getByRole("img", { name: "Ramón" });
  const firstFrame = sprite.style.backgroundPosition;

  act(() => vi.advanceTimersByTime(1200));
  expect(sprite.style.backgroundPosition).toBe(firstFrame);

  act(() => vi.advanceTimersByTime(400));
  expect(onComplete).toHaveBeenCalledOnce();
});
