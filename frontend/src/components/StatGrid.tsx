interface StatGridView {
  recentCount: number;
  avgKda: number;
  avgKills: number;
  avgDeaths: number;
  avgAssists: number;
  avgGpm: number;
  avgXpm: number;
  avgLastHits: number;
  wins: number;
  losses: number;
  winRate: number;
}

interface Props {
  view: StatGridView;
  modeLabel: string;
}

function StatCard({ label, value, accent }: { label: string; value: string; accent?: 'gold' | 'red' }) {
  const accentClass =
    accent === 'gold' ? 'text-dota-goldBright' : accent === 'red' ? 'text-dota-red' : 'text-dota-text';
  return (
    <div className="panel p-4 hover:border-dota-gold/50 transition-colors">
      <div className="stat-label">{label}</div>
      <div className={`stat-value mt-1 ${accentClass}`}>{value}</div>
    </div>
  );
}

export default function StatGrid({ view, modeLabel }: Props) {
  const hasRecent = view.recentCount > 0;
  return (
    <div>
      <h2 className="text-xl text-dota-gold mb-3">
        {modeLabel} 表现
        <span className="text-sm text-dota-muted font-sans ml-2 normal-case">
          （最近 20 场中 {view.recentCount} 场为此模式）
        </span>
      </h2>
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
        <StatCard label="Avg KDA" value={hasRecent ? view.avgKda.toFixed(2) : '—'} accent="gold" />
        <StatCard
          label="Avg K/D/A"
          value={hasRecent ? `${view.avgKills.toFixed(1)} / ${view.avgDeaths.toFixed(1)} / ${view.avgAssists.toFixed(1)}` : '—'}
        />
        <StatCard label="Avg GPM" value={hasRecent ? view.avgGpm.toFixed(0) : '—'} accent="gold" />
        <StatCard label="Avg XPM" value={hasRecent ? view.avgXpm.toFixed(0) : '—'} />
        <StatCard label="Avg Last Hits" value={hasRecent ? view.avgLastHits.toFixed(0) : '—'} />
        <StatCard label="模式胜率（生涯）" value={`${view.winRate.toFixed(1)}%`} accent="red" />
        <StatCard label="总胜场" value={view.wins.toString()} />
        <StatCard label="总负场" value={view.losses.toString()} />
      </div>
    </div>
  );
}
