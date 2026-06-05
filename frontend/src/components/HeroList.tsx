import type { HeroStat } from '../types';

interface Props {
  heroes: HeroStat[];
}

function winrateColor(wr: number) {
  if (wr >= 55) return 'text-dota-win';
  if (wr >= 45) return 'text-dota-gold';
  return 'text-dota-loss';
}

export default function HeroList({ heroes }: Props) {
  if (!heroes?.length) return null;
  return (
    <div>
      <h2 className="text-xl text-dota-gold mb-3">Top Heroes</h2>
      <div className="panel divide-y divide-dota-border">
        {heroes.map((h) => (
          <div key={h.heroId} className="flex items-center justify-between px-4 py-3 hover:bg-dota-panel2/50 transition-colors">
            <div className="flex-1">
              <div className="font-semibold text-dota-text">{h.heroName}</div>
              <div className="text-xs text-dota-muted">{h.games} games · {h.wins} wins</div>
            </div>
            <div className={`font-display text-lg font-semibold ${winrateColor(h.winRate)}`}>
              {h.winRate.toFixed(1)}%
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
