import { useEffect, useState } from 'react';
import { fetchMatchDetail } from '../api';
import type { MatchDetail, MatchPlayer } from '../types';
import { formatDuration, formatNumber, formatRelativeTime } from '../utils';
import LoadingSpinner from './LoadingSpinner';

interface Props {
  matchId: number;
  onClose: () => void;
}

function TeamTable({ players, label, winner }: { players: MatchPlayer[]; label: string; winner: boolean }) {
  const labelColor = label === '天辉' ? 'text-dota-win' : 'text-dota-red';
  return (
    <div>
      <div className="flex items-center justify-between mb-2">
        <h3 className={`font-display text-lg uppercase tracking-wider ${labelColor}`}>
          {label} {winner && <span className="text-xs ml-2 text-dota-gold">★ 胜</span>}
        </h3>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-xs">
          <thead>
            <tr className="text-left text-dota-muted uppercase tracking-wider border-b border-dota-border">
              <th className="px-2 py-2">英雄 / 玩家</th>
              <th className="px-2 py-2">Lv</th>
              <th className="px-2 py-2">K/D/A</th>
              <th className="px-2 py-2">LH/DN</th>
              <th className="px-2 py-2">GPM</th>
              <th className="px-2 py-2">XPM</th>
              <th className="px-2 py-2">净资产</th>
              <th className="px-2 py-2">英雄伤害</th>
              <th className="px-2 py-2">建筑伤害</th>
              <th className="px-2 py-2">治疗</th>
            </tr>
          </thead>
          <tbody>
            {players.map((p, i) => (
              <tr key={i} className="border-b border-dota-border/30">
                <td className="px-2 py-2">
                  <div className="font-semibold text-dota-text">{p.heroName}</div>
                  <div className="text-dota-muted truncate max-w-[150px]">{p.personaName ?? '匿名'}</div>
                </td>
                <td className="px-2 py-2 tabular-nums">{p.level}</td>
                <td className="px-2 py-2 tabular-nums">{p.kills}/{p.deaths}/{p.assists}</td>
                <td className="px-2 py-2 tabular-nums">{p.lastHits}/{p.denies}</td>
                <td className="px-2 py-2 tabular-nums text-dota-gold">{p.gpm}</td>
                <td className="px-2 py-2 tabular-nums">{p.xpm}</td>
                <td className="px-2 py-2 tabular-nums">{formatNumber(p.netWorth)}</td>
                <td className="px-2 py-2 tabular-nums">{formatNumber(p.heroDamage)}</td>
                <td className="px-2 py-2 tabular-nums">{formatNumber(p.towerDamage)}</td>
                <td className="px-2 py-2 tabular-nums">{formatNumber(p.heroHealing)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default function MatchDetailModal({ matchId, onClose }: Props) {
  const [data, setData] = useState<MatchDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let alive = true;
    setLoading(true);
    setError(null);
    fetchMatchDetail(matchId)
      .then((d) => { if (alive) setData(d); })
      .catch((e) => { if (alive) setError(e instanceof Error ? e.message : 'Unknown error'); })
      .finally(() => { if (alive) setLoading(false); });
    return () => { alive = false; };
  }, [matchId]);

  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose();
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  return (
    <div
      className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-start sm:items-center justify-center p-4 overflow-y-auto"
      onClick={onClose}
    >
      <div
        className="panel-glow w-full max-w-6xl my-8 p-5"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between mb-4">
          <div>
            <h2 className="font-display text-2xl text-dota-goldBright">
              比赛 #{matchId}
            </h2>
            {data && (
              <div className="text-sm text-dota-muted mt-1 flex flex-wrap gap-3">
                <span>{data.gameMode}</span>
                <span>· {data.lobbyType}</span>
                <span>· {formatDuration(data.duration)}</span>
                <span>· {formatRelativeTime(data.startTime)}</span>
              </div>
            )}
          </div>
          <button
            onClick={onClose}
            className="text-dota-muted hover:text-dota-red text-2xl leading-none px-2"
            title="关闭 (Esc)"
          >
            ×
          </button>
        </div>

        {loading && <LoadingSpinner label="加载比赛详情" />}
        {error && (
          <div className="panel p-4 border-l-4 border-l-dota-red">
            <p className="text-sm">{error}</p>
            <p className="text-xs text-dota-muted mt-2">某些较旧或未公开的比赛 OpenDota 可能没有数据。</p>
          </div>
        )}
        {data && (
          <div className="space-y-6">
            <div className="text-center font-display text-xl">
              <span className="text-dota-win">{data.radiantScore}</span>
              <span className="text-dota-muted mx-2">vs</span>
              <span className="text-dota-red">{data.direScore}</span>
            </div>
            <TeamTable players={data.radiant} label="天辉" winner={data.radiantWin} />
            <TeamTable players={data.dire} label="夜魇" winner={!data.radiantWin} />
            <div className="text-center">
              <a
                href={`https://www.opendota.com/matches/${matchId}`}
                target="_blank"
                rel="noreferrer"
                className="text-xs text-dota-muted hover:text-dota-gold transition-colors"
              >
                在 OpenDota 上查看完整数据 →
              </a>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
