import { useEffect, useMemo, useState } from 'react';
import { fetchSnapshot } from './api';
import type { MatchSummary, ModeKey, ModeStats, PlayerSnapshot } from './types';
import SearchBar from './components/SearchBar';
import PlayerHeader from './components/PlayerHeader';
import StatGrid from './components/StatGrid';
import HeroList from './components/HeroList';
import MatchList from './components/MatchList';
import LoadingSpinner from './components/LoadingSpinner';
import MatchDetailModal from './components/MatchDetailModal';
import HeroMatchups from './components/HeroMatchups';
import ModeTabs from './components/ModeTabs';
import {
  addHistory,
  loadHistory,
  removeHistory,
  type HistoryEntry,
} from './searchHistory';

const MODE_LABEL: Record<ModeKey, string> = {
  all: '全部',
  ranked: '天梯',
  turbo: 'Turbo',
  normal: '普通',
};

function pickMode(snap: PlayerSnapshot, mode: ModeKey): ModeStats {
  if (mode === 'ranked') return snap.ranked ?? emptyMode();
  if (mode === 'turbo') return snap.turbo ?? emptyMode();
  if (mode === 'normal') return snap.normal ?? emptyMode();
  return {
    wins: snap.wins,
    losses: snap.losses,
    winRate: snap.winRate,
    recentCount: snap.recentMatches.length,
    avgKills: snap.avgKills,
    avgDeaths: snap.avgDeaths,
    avgAssists: snap.avgAssists,
    avgKda: snap.avgKda,
    avgGpm: snap.avgGpm,
    avgXpm: snap.avgXpm,
    avgLastHits: snap.avgLastHits,
    topHeroes: snap.topHeroes,
  };
}

function emptyMode(): ModeStats {
  return {
    wins: 0, losses: 0, winRate: 0, recentCount: 0,
    avgKills: 0, avgDeaths: 0, avgAssists: 0, avgKda: 0,
    avgGpm: 0, avgXpm: 0, avgLastHits: 0, topHeroes: [],
  };
}

function filterMatchesByMode(matches: MatchSummary[], mode: ModeKey): MatchSummary[] {
  if (mode === 'all') return matches;
  if (mode === 'turbo') return matches.filter((m) => m.gameMode === 'Turbo');
  if (mode === 'ranked') return matches.filter((m) => m.lobbyType === 'Ranked' && m.gameMode !== 'Turbo');
  return matches.filter((m) => m.lobbyType !== 'Ranked' && m.gameMode !== 'Turbo');
}

export default function App() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [snap, setSnap] = useState<PlayerSnapshot | null>(null);
  const [history, setHistory] = useState<HistoryEntry[]>([]);
  const [openMatchId, setOpenMatchId] = useState<number | null>(null);
  const [mode, setMode] = useState<ModeKey>('all');

  useEffect(() => {
    setHistory(loadHistory());
  }, []);

  const view = useMemo(() => snap ? pickMode(snap, mode) : null, [snap, mode]);
  const filteredMatches = useMemo(
    () => snap ? filterMatchesByMode(snap.recentMatches, mode) : [],
    [snap, mode]
  );

  async function handleSubmit(steamId: string) {
    setLoading(true);
    setError(null);
    setSnap(null);
    setMode('all');
    try {
      const result = await fetchSnapshot(steamId);
      setSnap(result);
      const entry: HistoryEntry = {
        steamId,
        personaName: result.personaName,
        avatar: result.avatar,
        ts: Date.now(),
      };
      setHistory(addHistory(entry));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  }

  function handleClearHistory(steamId: string) {
    setHistory(removeHistory(steamId));
  }

  const topHero = snap?.topHeroes?.[0];

  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b border-dota-border bg-dota-panel/60 backdrop-blur">
        <div className="max-w-6xl mx-auto px-4 py-5 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-md bg-gradient-to-br from-dota-red to-dota-gold flex items-center justify-center shadow-redGlow">
              <span className="font-display font-bold text-white text-lg">D</span>
            </div>
            <div>
              <h1 className="font-display text-2xl font-bold text-dota-goldBright leading-none">
                DotaLens
              </h1>
              <div className="text-xs text-dota-muted uppercase tracking-widest">
                Dota 2 战绩查询
              </div>
            </div>
          </div>
          <a
            href="https://www.opendota.com"
            target="_blank"
            rel="noreferrer"
            className="text-xs text-dota-muted hover:text-dota-gold transition-colors hidden sm:block"
          >
            数据来源：OpenDota
          </a>
        </div>
      </header>

      <main className="flex-1 max-w-6xl w-full mx-auto px-4 py-8 space-y-8">
        <section className="text-center space-y-4">
          <h2 className="text-3xl sm:text-4xl font-display font-bold text-dota-text">
            查看 <span className="text-dota-red">Dota 2</span> 玩家战绩
          </h2>
          <p className="text-dota-muted max-w-2xl mx-auto">
            输入 Steam ID（Steam64 会自动转换）查看最近 20 场战绩、英雄统计、段位和 MMR。
          </p>
          <SearchBar
            onSubmit={handleSubmit}
            loading={loading}
            history={history}
            onClearHistory={handleClearHistory}
          />
        </section>

        {loading && <LoadingSpinner label="拉取数据中" />}

        {error && (
          <div className="panel p-5 border-l-4 border-l-dota-red max-w-2xl mx-auto">
            <h3 className="font-display text-dota-red uppercase tracking-wider mb-1">出错了</h3>
            <p className="text-sm">{error}</p>
            <p className="text-xs text-dota-muted mt-2">
              提示：玩家需要在 Dota 2 客户端中开启 "公开比赛数据"，OpenDota 才能查到。
            </p>
          </div>
        )}

        {snap && view && (
          <>
            <PlayerHeader snap={snap} />
            <ModeTabs snap={snap} mode={mode} onChange={setMode} />
            <StatGrid view={view} modeLabel={MODE_LABEL[mode]} />
            <div className="grid lg:grid-cols-3 gap-6">
              <div className="lg:col-span-1">
                <HeroList heroes={view.topHeroes} />
              </div>
              <div className="lg:col-span-2">
                <MatchList matches={filteredMatches} onSelect={setOpenMatchId} />
              </div>
            </div>
            <HeroMatchups
              defaultHeroId={topHero?.heroId}
              defaultHeroName={topHero?.heroName}
            />
          </>
        )}

        {!snap && !loading && !error && (
          <HeroMatchups />
        )}
      </main>

      {openMatchId !== null && (
        <MatchDetailModal matchId={openMatchId} onClose={() => setOpenMatchId(null)} />
      )}

      <footer className="border-t border-dota-border py-4 text-center text-xs text-dota-muted">
        数据来源 <a href="https://www.opendota.com" target="_blank" rel="noreferrer" className="hover:text-dota-gold">OpenDota</a> · 与 Valve 无关
      </footer>
    </div>
  );
}
