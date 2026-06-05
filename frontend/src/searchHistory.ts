const KEY = 'dotalens.searchHistory.v1';
const MAX = 6;

export interface HistoryEntry {
  steamId: string;
  personaName?: string;
  avatar?: string;
  ts: number;
}

export function loadHistory(): HistoryEntry[] {
  try {
    const raw = localStorage.getItem(KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed;
  } catch {
    return [];
  }
}

export function addHistory(entry: HistoryEntry): HistoryEntry[] {
  const existing = loadHistory().filter((e) => e.steamId !== entry.steamId);
  const next = [entry, ...existing].slice(0, MAX);
  localStorage.setItem(KEY, JSON.stringify(next));
  return next;
}

export function removeHistory(steamId: string): HistoryEntry[] {
  const next = loadHistory().filter((e) => e.steamId !== steamId);
  localStorage.setItem(KEY, JSON.stringify(next));
  return next;
}
