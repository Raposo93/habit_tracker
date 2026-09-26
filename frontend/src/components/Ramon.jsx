import { useEffect, useRef, useState } from "react";

const SPRITESHEET = {
  src: "/assets/ramon/spritesheet.png",
  columns: 8,
  rows: 11,
};

const ANIMATIONS = {
  // Idle stays on its first frame to keep the companion quiet between reactions.
  idle: { ...SPRITESHEET, row: 0, frames: 6 },
  walkLeft: { ...SPRITESHEET, row: 2, frames: 8, duration: 650 },
  happy: { ...SPRITESHEET, row: 6, frames: 6, duration: 900 },
  sleepy: { ...SPRITESHEET, row: 5, frames: 8, duration: 1000 },
  apple: {
    src: "/assets/ramon-apple-sprite.png",
    columns: 4,
    rows: 2,
    frames: 8,
    duration: 1600,
  },
};

function prefersReducedMotion() {
  return window.matchMedia?.("(prefers-reduced-motion: reduce)").matches ?? false;
}

export default function Ramon({ animation = "idle", label, onComplete, playKey }) {
  const [frame, setFrame] = useState(0);
  const [reducedMotion, setReducedMotion] = useState(prefersReducedMotion);
  const onCompleteRef = useRef(onComplete);
  onCompleteRef.current = onComplete;
  const selected = ANIMATIONS[animation] ?? ANIMATIONS.idle;

  useEffect(() => {
    const preference = window.matchMedia?.("(prefers-reduced-motion: reduce)");
    if (!preference) return undefined;
    const update = () => setReducedMotion(preference.matches);
    preference.addEventListener("change", update);
    return () => preference.removeEventListener("change", update);
  }, []);

  useEffect(() => {
    setFrame(0);
    if (!selected.duration) return undefined;

    const interval = reducedMotion
      ? null
      : window.setInterval(
          () => setFrame((current) => Math.min(current + 1, selected.frames - 1)),
          selected.duration / selected.frames,
        );
    // Completion also runs with reduced motion, independently of CSS events.
    const completion = window.setTimeout(
      () => onCompleteRef.current?.(),
      selected.duration,
    );

    return () => {
      if (interval !== null) window.clearInterval(interval);
      window.clearTimeout(completion);
    };
  }, [animation, playKey, reducedMotion, selected]);

  const shownFrame = reducedMotion ? 0 : frame;
  const column = shownFrame % selected.columns;
  const row = selected.row ?? Math.floor(shownFrame / selected.columns);

  return (
    <span
      className="ramon-sprite"
      role={label ? "img" : undefined}
      aria-label={label}
      aria-hidden={label ? undefined : "true"}
      data-animation={animation}
      style={{
        backgroundImage: `url("${selected.src}")`,
        backgroundSize: `${selected.columns * 100}% ${selected.rows * 100}%`,
        backgroundPosition: `${(column / (selected.columns - 1)) * 100}% ${(row / (selected.rows - 1)) * 100}%`,
      }}
    />
  );
}
