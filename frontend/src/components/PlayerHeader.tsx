import type { PlayerSnapshot } from '../types';

interface Props {
  snap: PlayerSnapshot;
}

const RANK_NAMES: Record<number, string> = {
  1: 'Herald', 2: 'Guardian', 3: 'Crusader', 4: 'Archon',
  5: 'Legend', 6: 'Ancient', 7: 'Divine', 8: 'Immortal',
};

function rankLabel(rankTier?: number, leaderboard?: number) {
  if (!rankTier) return 'Unranked';
  const tier = Math.floor(rankTier / 10);
  const stars = rankTier % 10;
  const base = RANK_NAMES[tier] ?? `Tier ${tier}`;
  if (tier === 8 && leaderboard) return `Immortal #${leaderboard}`;
  if (tier === 8) return 'Immortal';
  return `${base}${stars ? ` ${stars}` : ''}`;
}

export default function PlayerHeader({ snap }: Props) {
  return (
    <div className="panel-glow p-6 flex flex-col sm:flex-row items-center gap-5">
      {snap.avatar && (
        <img
          src={snap.avatar}
          alt={snap.personaName ?? 'avatar'}
          className="w-24 h-24 rounded-md border-2 border-dota-gold shadow-glow"
        />
      )}
      <div className="flex-1 text-center sm:text-left">
        <h1 className="text-3xl font-display font-bold text-dota-goldBright">
          {snap.personaName ?? `Player ${snap.steamId}`}
        </h1>
        <div className="text-sm text-dota-muted mt-1">Steam ID: {snap.steamId}</div>
        <div className="mt-2 flex flex-wrap items-center justify-center sm:justify-start gap-3">
          <span className="inline-flex items-center gap-2 bg-dota-panel2 px-3 py-1 rounded-md border border-dota-border">
            <span className="text-dota-gold text-xs uppercase tracking-wider">Rank</span>
            <span className="font-semibold">{rankLabel(snap.rankTier, snap.leaderboardRank)}</span>
          </span>
          {snap.mmrEstimate ? (
            <span className="inline-flex items-center gap-2 bg-dota-panel2 px-3 py-1 rounded-md border border-dota-border">
              <span className="text-dota-gold text-xs uppercase tracking-wider">MMR ~</span>
              <span className="font-semibold">{snap.mmrEstimate}</span>
            </span>
          ) : null}
          <span className="inline-flex items-center gap-2 bg-dota-panel2 px-3 py-1 rounded-md border border-dota-border">
            <span className="text-dota-win">{snap.wins}W</span>
            <span className="text-dota-muted">/</span>
            <span className="text-dota-loss">{snap.losses}L</span>
            <span className="text-dota-muted">·</span>
            <span className="font-semibold">{snap.winRate.toFixed(1)}%</span>
          </span>
        </div>
      </div>
    </div>
  );
}
