const DAILY_ENTRIES_URL = "/api/entries";

async function ensureSuccess(response) {
  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`);
  }

  return response;
}

export async function loadDailyEntryContext(date) {
  const response = await ensureSuccess(
    await fetch(`${DAILY_ENTRIES_URL}/context?date=${encodeURIComponent(date)}`),
  );

  return response.json();
}

export async function createDailyEntry(date, habitId, entry) {
  await ensureSuccess(
    await fetch(entryUrl(date, habitId), {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(entry),
    }),
  );
}

export async function updateDailyEntry(date, habitId, entry) {
  await ensureSuccess(
    await fetch(entryUrl(date, habitId), {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(entry),
    }),
  );
}

function entryUrl(date, habitId) {
  return `${DAILY_ENTRIES_URL}/${encodeURIComponent(date)}/${encodeURIComponent(habitId)}`;
}
