const HABITS_URL = "/api/habits";

export class HabitApiError extends Error {
  constructor(code, message, status = null, cause) {
    super(message);
    this.name = "HabitApiError";
    this.code = code;
    this.status = status;
    this.cause = cause;
  }
}

async function request(url, options) {
  let response;
  try {
    response = options === undefined ? await fetch(url) : await fetch(url, options);
  } catch (cause) {
    throw new HabitApiError("BACKEND_UNAVAILABLE", "Could not connect to the server", null, cause);
  }
  if (!response.ok) {
    let body;
    try { body = await response.json(); } catch { body = null; }
    throw new HabitApiError(body?.code ?? "HTTP_ERROR", body?.message ?? "Habit request failed", response.status);
  }
  return response;
}

export async function loadHabits() {
  const response = await request(HABITS_URL);
  try {
    const body = await response.json();
    if (!Array.isArray(body.habits)) throw new Error("Missing habit catalog");
    return body.habits;
  } catch (cause) {
    throw new HabitApiError("INVALID_RESPONSE", "Invalid habit catalog", response.status, cause);
  }
}

function write(url, method, body) {
  return request(url, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
}

export async function createHabit(habit) {
  await write(HABITS_URL, "POST", habit);
}

export async function renameHabit(habitId, habitName) {
  await write(`${HABITS_URL}/${encodeURIComponent(habitId)}/name`, "PUT", { habitName });
}

export async function setHabitActive(habitId, active) {
  await write(`${HABITS_URL}/${encodeURIComponent(habitId)}/active`, "PUT", { active });
}

export async function reorderHabits(habitIds) {
  await write(`${HABITS_URL}/order`, "PUT", { habitIds });
}
