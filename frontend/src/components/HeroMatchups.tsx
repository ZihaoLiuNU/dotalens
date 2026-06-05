import { useEffect, useState } from 'react';
import { fetchHeroes, fetchHeroMatchups } from '../api';
import type { HeroMatchup, HeroSimple } from '../types';

interface Props {
  defaultHeroId?: number;
  defaultHeroName?: string;
}

function disadvantageColor(d: number) {
  if (d >= 3) return 'text-dota-red';
  if (d >= 1) return 'text-dota-gold';
  if (d <= -3) return 'text-dota-win';
  return 'text-dota-text';
}

export default function HeroMatchups({ defaultHeroId, defaultHeroName }: Props) {
  const [heroes, setHeroes] = useState<HeroSimple[]>([]);
  const [selected, setSelected] = useState<number | null>(defaultHeroId ?? null);
  const [matchups, setMatchups] = useState<HeroMatchup[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchHeroes()
      .then(setHeroes)
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to load heroes'));
  }, []);

  useEffect(() => {
    if (!selected) return;
    setLoading(true);
    setError(null);
    fetchHeroMatchups(selected)
      .then(setMatchups)
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed'))
      .finally(() => setLoading(false));
  }, [selected]);

  const selectedName = heroes.find((h) => h.heroId === selected)?.heroName ?? defaultHeroName ?? '';

  const worst = matchups.slice(0, 8);
  const best = [...matchups].reverse().slice(0, 8);

  return (
    <div>
      <h2 className="text-xl text-dota-gold mb-3">英雄克制查询</h2>
      <div className="panel p-4 space-y-4">
        <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center">
          <label className="text-sm text-dota-muted">选择英雄：</label>
          <select
            className="input-dark flex-1 max-w-xs"
            value={selected ?? ''}
            onChange={(e) => setSelected(e.target.value ? Number(e.target.value) : null)}
          >
            <option value="">-- 选择一个英雄 --</option>
            {heroes.map((h) => (
              <option key={h.heroId} value={h.heroId}>{h.heroName}</option>
            ))}
          </select>
          {defaultHeroId && selected !== defaultHeroId && (
            <button
              type="button"
              onClick={() => setSelected(defaultHeroId)}
              className="text-xs text-dota-gold hover:text-dota-goldBright underline"
            >
              ← 使用最常玩英雄 ({defaultHeroName})
            </button>
          )}
        </div>

        {error && <p className="text-sm text-dota-red">{error}</p>}
        {loading && <p className="text-sm text-dota-muted">加载中…</p>}

        {!loading && matchups.length > 0 && (
          <>
            <p className="text-xs text-dota-muted">
              基于 <span className="text-dota-text">{selectedName}</span> 在 OpenDota 上至少 50 场对阵的样本。
              劣势 = 50% - 你的胜率（数字越大越克）。
            </p>
            <div className="grid md:grid-cols-2 gap-4">
              <div>
                <h3 className="font-display text-dota-red uppercase tracking-wider text-sm mb-2">最克制你的英雄</h3>
                <div className="divide-y divide-dota-border/40">
                  {worst.map((m) => (
                    <div key={m.heroId} className="flex items-center justify-between py-2">
                      <span className="text-sm">{m.heroName}</span>
                      <div className="text-right">
                        <div className={`font-display font-semibold ${disadvantageColor(m.disadvantage)}`}>
                          {m.disadvantage > 0 ? '+' : ''}{m.disadvantage.toFixed(2)}%
                        </div>
                        <div className="text-xs text-dota-muted">
                          {m.winRate.toFixed(1)}% · {m.gamesPlayed} 场
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
              <div>
                <h3 className="font-display text-dota-win uppercase tracking-wider text-sm mb-2">你最克制的英雄</h3>
                <div className="divide-y divide-dota-border/40">
                  {best.map((m) => (
                    <div key={m.heroId} className="flex items-center justify-between py-2">
                      <span className="text-sm">{m.heroName}</span>
                      <div className="text-right">
                        <div className={`font-display font-semibold ${disadvantageColor(m.disadvantage)}`}>
                          {m.disadvantage > 0 ? '+' : ''}{m.disadvantage.toFixed(2)}%
                        </div>
                        <div className="text-xs text-dota-muted">
                          {m.winRate.toFixed(1)}% · {m.gamesPlayed} 场
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </>
        )}

        {!loading && selected && matchups.length === 0 && !error && (
          <p className="text-sm text-dota-muted">没有对阵数据。</p>
        )}
      </div>
    </div>
  );
}
