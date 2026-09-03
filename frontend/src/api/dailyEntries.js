const DAILY_ENTRIES_URL = "/api/entries";

export class DailyEntryApiError extends Error {
  constructor({ code, message, status = null, cause }) {
    super(message);
    this.name = "DailyEntryApiError";
    this.code = code;
    this.status = status;
    this.cause = cause;
  }
}

async function request(url, options) {
  let response;

  try {
    response =
      options === undefined ? await fetch(url) : await fetch(url, options);
  } catch (cause) {
    throw new DailyEntryApiError({
      code: "BACKEND_UNAVAILABLE",
      message: "Could not connect to the server",
      cause,
    });
  }

  if (!response.ok) {
    const errorBody = await readErrorBody(response);

    throw new DailyEntryApiError({
      code: errorBody?.code ?? "HTTP_ERROR",
      message:
        errorBody?.message ?? `Request failed with status ${response.status}`,
      status: response.status,
    });
  }

  return response;
}

async function readErrorBody(response) {
  try {
    return await response.json();
  } catch {
    return null;
  }
}

export async function loadDailyEntryContext(date) {
  const response = await request(
    `${DAILY_ENTRIES_URL}/context?date=${encodeURIComponent(date)}`,
  );

  try {
    return await response.json();
  } catch (cause) {
    throw new DailyEntryApiError({
      code: "INVALID_RESPONSE",
      message: "The server returned an invalid response",
      status: response.status,
      cause,
    });
  }
}

export async function createDailyEntry(date, habitId, entry) {
  await request(entryUrl(date, habitId), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(entry),
  });
}

export async function updateDailyEntry(date, habitId, entry) {
  await request(entryUrl(date, habitId), {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(entry),
  });
}

function entryUrl(date, habitId) {
  return `${DAILY_ENTRIES_URL}/${encodeURIComponent(date)}/${encodeURIComponent(habitId)}`;
}
