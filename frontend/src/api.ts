import type { HeroMatchup, HeroSimple, MatchDetail, PlayerSnapshot } from './types';

const BASE = '/api';

async function handle<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      const body = await res.json();
      if (body?.error) msg = body.error;
    } catch {
      // ignore
    }
    throw new Error(msg);
  }
  return res.json() as Promise<T>;
}

export async function fetchSnapshot(steamId: string): Promise<PlayerSnapshot> {
  const res = await fetch(`${BASE}/snapshot/${encodeURIComponent(steamId)}`);
  return handle<PlayerSnapshot>(res);
}

export async function fetchMatchDetail(matchId: number): Promise<MatchDetail> {
  const res = await fetch(`${BASE}/match/${matchId}`);
  return handle<MatchDetail>(res);
}

export async function fetchHeroes(): Promise<HeroSimple[]> {
  const res = await fetch(`${BASE}/heroes`);
  return handle<HeroSimple[]>(res);
}

export async function fetchHeroMatchups(heroId: number): Promise<HeroMatchup[]> {
  const res = await fetch(`${BASE}/heroes/${heroId}/matchups`);
  return handle<HeroMatchup[]>(res);
}
