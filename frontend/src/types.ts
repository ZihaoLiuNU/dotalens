export interface HeroStat {
  heroId: number;
  heroName: string;
  games: number;
  wins: number;
  winRate: number;
}

export interface MatchSummary {
  matchId: number;
  heroId: number;
  heroName: string;
  kills: number;
  deaths: number;
  assists: number;
  gpm: number;
  xpm: number;
  lastHits: number;
  duration: number;
  win: boolean;
  startTime: number;
  lobbyType: string;
  gameMode: string;
}

export interface ModeStats {
  wins: number;
  losses: number;
  winRate: number;
  recentCount: number;
  avgKills: number;
  avgDeaths: number;
  avgAssists: number;
  avgKda: number;
  avgGpm: number;
  avgXpm: number;
  avgLastHits: number;
  topHeroes: HeroStat[];
}

export interface PlayerSnapshot {
  steamId: number;
  personaName?: string;
  avatar?: string;
  profileUrl?: string;
  rankTier?: number;
  leaderboardRank?: number;
  mmrEstimate?: number;
  wins: number;
  losses: number;
  winRate: number;
  avgKills: number;
  avgDeaths: number;
  avgAssists: number;
  avgKda: number;
  avgGpm: number;
  avgXpm: number;
  avgLastHits: number;
  topHeroes: HeroStat[];
  recentMatches: MatchSummary[];
  ranked?: ModeStats;
  turbo?: ModeStats;
  normal?: ModeStats;
}

export type ModeKey = 'all' | 'ranked' | 'turbo' | 'normal';

export interface AnalysisResult {
  weakness: string;
  strength: string;
  improvements: string[];
  hero_advice: string;
  playstyle_summary: string;
}

export interface AnalysisResponse {
  snapshot: PlayerSnapshot;
  analysis?: AnalysisResult;
  analysisError?: string;
}

export interface MatchPlayer {
  accountId?: number;
  personaName?: string;
  heroId: number;
  heroName: string;
  level: number;
  kills: number;
  deaths: number;
  assists: number;
  lastHits: number;
  denies: number;
  gpm: number;
  xpm: number;
  netWorth: number;
  heroDamage: number;
  towerDamage: number;
  heroHealing: number;
  items: number[];
}

export interface MatchDetail {
  matchId: number;
  duration: number;
  startTime: number;
  radiantWin: boolean;
  radiantScore: number;
  direScore: number;
  gameMode: string;
  lobbyType: string;
  radiant: MatchPlayer[];
  dire: MatchPlayer[];
}

export interface HeroMatchup {
  heroId: number;
  heroName: string;
  gamesPlayed: number;
  wins: number;
  winRate: number;
  disadvantage: number;
}

export interface HeroSimple {
  heroId: number;
  heroName: string;
}
