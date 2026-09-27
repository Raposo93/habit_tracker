import { act, render, screen } from "@testing-library/react";
import { readFileSync } from "node:fs";
import { afterEach, expect, it, vi } from "vitest";

import Ramon from "./Ramon.jsx";

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

it("uses transparent apple cells with the same proportions as idle", () => {
  const image = readFileSync("public/assets/ramon-apple-sprite.png");
  // PNG IHDR: dimensions and color type (6 = RGBA).
  const width = image.readUInt32BE(16);
  const height = image.readUInt32BE(20);

  expect(width / 4).toBe(192);
  expect(height / 2).toBe(208);
  expect(image[25]).toBe(6);
});

it("plays all eight apple poses across both rows and returns to idle", () => {
  vi.useFakeTimers();
  const onComplete = vi.fn();
  const { rerender } = render(<Ramon animation="apple" label="Ramón" onComplete={onComplete} />);
  const sprite = screen.getByRole("img", { name: "Ramón" });

  expect(sprite.style.backgroundSize).toBe("400% 200%");
  for (let frame = 0; frame < 8; frame += 1) {
    if (frame > 0) act(() => vi.advanceTimersByTime(200));
    expect(sprite.style.backgroundPosition).toBe(
      `${((frame % 4) / 3) * 100}% ${Math.floor(frame / 4) * 100}%`,
    );
    expect(onComplete).not.toHaveBeenCalled();
  }

  act(() => vi.advanceTimersByTime(200));
  expect(onComplete).toHaveBeenCalledOnce();
  rerender(<Ramon animation="idle" label="Ramón" />);
  expect(sprite.style.backgroundPosition).toBe("0% 0%");
  expect(sprite.style.backgroundImage).toContain("/assets/ramon/spritesheet.png");
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
