const STEAM64_OFFSET = 76561197960265728n;

export function normalizeSteamId(input: string): string {
  const trimmed = input.trim();
  if (!/^\d+$/.test(trimmed)) return trimmed;
  if (trimmed.length >= 17 && trimmed.startsWith('7656119')) {
    try {
      const id64 = BigInt(trimmed);
      const id32 = id64 - STEAM64_OFFSET;
      if (id32 > 0n) return id32.toString();
    } catch {
      return trimmed;
    }
  }
  return trimmed;
}

export function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${s.toString().padStart(2, '0')}`;
}

export function formatRelativeTime(startTime: number): string {
  if (!startTime) return '';
  const diffSec = Math.floor(Date.now() / 1000 - startTime);
  if (diffSec < 60) return `${diffSec}s ago`;
  const h = Math.floor(diffSec / 3600);
  if (h < 1) return `${Math.floor(diffSec / 60)}m ago`;
  if (h < 24) return `${h}h ago`;
  return `${Math.floor(h / 24)}d ago`;
}

export function formatNumber(n: number): string {
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k';
  return n.toString();
}
