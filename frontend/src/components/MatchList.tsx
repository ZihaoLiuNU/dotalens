import type { MatchSummary } from '../types';
import { formatDuration, formatRelativeTime } from '../utils';

interface Props {
  matches: MatchSummary[];
  onSelect: (matchId: number) => void;
}

export default function MatchList({ matches, onSelect }: Props) {
  if (!matches?.length) return null;
  return (
    <div>
      <h2 className="text-xl text-dota-gold mb-3">最近 {matches.length} 场比赛</h2>
      <div className="panel overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-dota-muted text-xs uppercase tracking-wider border-b border-dota-border">
              <th className="px-4 py-3">结果</th>
              <th className="px-4 py-3">英雄</th>
              <th className="px-4 py-3">K / D / A</th>
              <th className="px-4 py-3">GPM</th>
              <th className="px-4 py-3">XPM</th>
              <th className="px-4 py-3">LH</th>
              <th className="px-4 py-3">模式</th>
              <th className="px-4 py-3">时长</th>
              <th className="px-4 py-3 hidden md:table-cell">时间</th>
            </tr>
          </thead>
          <tbody>
            {matches.map((m) => (
              <tr
                key={m.matchId}
                onClick={() => onSelect(m.matchId)}
                className="border-b border-dota-border/40 hover:bg-dota-panel2 cursor-pointer transition-colors"
              >
                <td className={`px-4 py-3 font-display font-semibold ${m.win ? 'text-dota-win' : 'text-dota-loss'}`}>
                  {m.win ? '胜' : '负'}
                </td>
                <td className="px-4 py-3">{m.heroName}</td>
                <td className="px-4 py-3 tabular-nums">{m.kills} / {m.deaths} / {m.assists}</td>
                <td className="px-4 py-3 tabular-nums text-dota-gold">{m.gpm}</td>
                <td className="px-4 py-3 tabular-nums">{m.xpm}</td>
                <td className="px-4 py-3 tabular-nums">{m.lastHits}</td>
                <td className="px-4 py-3 text-dota-muted text-xs">{m.gameMode}</td>
                <td className="px-4 py-3 tabular-nums">{formatDuration(m.duration)}</td>
                <td className="px-4 py-3 text-dota-muted text-xs hidden md:table-cell">{formatRelativeTime(m.startTime)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="text-xs text-dota-muted mt-2">点击任意一行查看比赛详情</p>
    </div>
  );
}
