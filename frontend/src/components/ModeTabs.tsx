import type { ModeKey, PlayerSnapshot } from '../types';

interface Props {
  snap: PlayerSnapshot;
  mode: ModeKey;
  onChange: (mode: ModeKey) => void;
}

interface Tab {
  key: ModeKey;
  label: string;
  count: number;
}

export default function ModeTabs({ snap, mode, onChange }: Props) {
  const tabs: Tab[] = [
    {
      key: 'all',
      label: '全部',
      count: (snap.wins ?? 0) + (snap.losses ?? 0),
    },
    {
      key: 'ranked',
      label: '天梯',
      count: (snap.ranked?.wins ?? 0) + (snap.ranked?.losses ?? 0),
    },
    {
      key: 'turbo',
      label: 'Turbo',
      count: (snap.turbo?.wins ?? 0) + (snap.turbo?.losses ?? 0),
    },
    {
      key: 'normal',
      label: '普通',
      count: (snap.normal?.wins ?? 0) + (snap.normal?.losses ?? 0),
    },
  ];

  return (
    <div className="flex flex-wrap gap-1 border-b border-dota-border">
      {tabs.map((t) => {
        const active = mode === t.key;
        return (
          <button
            key={t.key}
            onClick={() => onChange(t.key)}
            className={`px-5 py-3 font-display uppercase tracking-wider text-sm transition-colors relative ${
              active
                ? 'text-dota-goldBright'
                : 'text-dota-muted hover:text-dota-text'
            }`}
          >
            {t.label}
            <span className="ml-2 text-xs opacity-70">({t.count})</span>
            {active && (
              <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-dota-gold" />
            )}
          </button>
        );
      })}
    </div>
  );
}
