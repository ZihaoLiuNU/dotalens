import { FormEvent, useState } from 'react';
import { isVanityUrl, normalizeSteamId } from '../utils';
import type { HistoryEntry } from '../searchHistory';

interface Props {
  onSubmit: (steamId: string) => void;
  loading: boolean;
  history: HistoryEntry[];
  onClearHistory: (steamId: string) => void;
}

export default function SearchBar({ onSubmit, loading, history, onClearHistory }: Props) {
  const [value, setValue] = useState('');

  function submit(raw: string) {
    const normalized = normalizeSteamId(raw);
    if (normalized) onSubmit(normalized);
  }

  function handle(e: FormEvent) {
    e.preventDefault();
    const trimmed = value.trim();
    if (trimmed) submit(trimmed);
  }

  const vanityWarning = value.trim() && isVanityUrl(value);

  return (
    <div className="w-full max-w-2xl mx-auto space-y-3">
      <form onSubmit={handle} className="flex flex-col sm:flex-row gap-3">
        <input
          className="input-dark flex-1"
          placeholder="输入 Steam ID 或粘贴 Steam / OpenDota / Dotabuff 资料链接"
          value={value}
          onChange={(e) => setValue(e.target.value)}
          disabled={loading}
          autoFocus
        />
        <button type="submit" className="btn-primary whitespace-nowrap" disabled={loading || !value.trim() || !!vanityWarning}>
          {loading ? '查询中…' : '查询战绩'}
        </button>
      </form>

      {vanityWarning && (
        <p className="text-xs text-dota-red text-left">
          这是 Steam 自定义 URL（/id/xxx），需要 Steam API key 才能解析。请在 Steam 客户端 → 个人资料 → 编辑资料里查看 17 位数字 ID，或直接用 /profiles/数字 那种链接。
        </p>
      )}

      {history.length > 0 && (
        <div className="flex flex-wrap gap-2 justify-center sm:justify-start">
          <span className="text-xs text-dota-muted uppercase tracking-wider self-center mr-1">
            最近搜索
          </span>
          {history.map((h) => (
            <div
              key={h.steamId}
              className="group inline-flex items-center gap-2 bg-dota-panel2 border border-dota-border hover:border-dota-gold rounded-md pl-1 pr-1 py-1 transition-colors"
            >
              <button
                type="button"
                className="flex items-center gap-2 text-sm"
                onClick={() => submit(h.steamId)}
                disabled={loading}
                title={`Steam ID: ${h.steamId}`}
              >
                {h.avatar ? (
                  <img src={h.avatar} alt="" className="w-6 h-6 rounded-sm" />
                ) : (
                  <div className="w-6 h-6 rounded-sm bg-dota-border" />
                )}
                <span className="text-dota-text">{h.personaName ?? h.steamId}</span>
              </button>
              <button
                type="button"
                onClick={() => onClearHistory(h.steamId)}
                className="text-dota-muted hover:text-dota-red text-xs px-1"
                title="移除"
              >
                ×
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
