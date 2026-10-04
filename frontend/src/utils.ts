const STEAM64_OFFSET = 76561197960265728n;

function steam64ToSteam32(id64Str: string): string | null {
  try {
    const id64 = BigInt(id64Str);
    const id32 = id64 - STEAM64_OFFSET;
    if (id32 > 0n) return id32.toString();
  } catch {
    // not a valid bigint
  }
  return null;
}

export function isVanityUrl(input: string): boolean {
  return /steamcommunity\.com\/id\/[^/?#]+/i.test(input.trim());
}

export function normalizeSteamId(input: string): string {
  const s = input.trim();

  // Steam community profile URL: /profiles/{steam64}
  const profileMatch = s.match(/\/profiles\/(\d{17,})/);
  if (profileMatch) {
    return steam64ToSteam32(profileMatch[1]) ?? profileMatch[1];
  }

  // OpenDota / Dotabuff URL: /players/{id}
  const playerMatch = s.match(/\/players\/(\d+)/);
  if (playerMatch) {
    const id = playerMatch[1];
    if (id.length >= 17 && id.startsWith('7656119')) {
      return steam64ToSteam32(id) ?? id;
    }
    return id;
  }

  // Bare numeric ID — convert if Steam64-shaped
  if (/^\d+$/.test(s)) {
    if (s.length >= 17 && s.startsWith('7656119')) {
      return steam64ToSteam32(s) ?? s;
    }
    return s;
  }

  return s;
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
