import type { AnalysisResult } from '../types';

interface Props {
  analysis?: AnalysisResult;
  error?: string;
}

function Card({ title, accent = 'gold', children }: { title: string; accent?: 'gold' | 'red' | 'win'; children: React.ReactNode }) {
  const borderClass =
    accent === 'red' ? 'border-l-dota-red' : accent === 'win' ? 'border-l-dota-win' : 'border-l-dota-gold';
  const titleClass =
    accent === 'red' ? 'text-dota-red' : accent === 'win' ? 'text-dota-win' : 'text-dota-goldBright';
  return (
    <div className={`panel p-5 border-l-4 ${borderClass}`}>
      <h3 className={`font-display text-lg uppercase tracking-wider mb-2 ${titleClass}`}>{title}</h3>
      <div className="text-dota-text text-sm leading-relaxed">{children}</div>
    </div>
  );
}

export default function AnalysisCards({ analysis, error }: Props) {
  if (error) {
    return (
      <div>
        <h2 className="text-xl text-dota-gold mb-3">AI Coach</h2>
        <div className="panel p-5 border-l-4 border-l-dota-red">
          <h3 className="font-display text-lg uppercase tracking-wider mb-2 text-dota-red">Analysis Unavailable</h3>
          <p className="text-sm text-dota-muted">{error}</p>
          <p className="text-xs text-dota-muted mt-2">
            Make sure <code className="bg-dota-panel2 px-1 rounded">ANTHROPIC_API_KEY</code> is set in the backend environment.
          </p>
        </div>
      </div>
    );
  }
  if (!analysis) return null;

  return (
    <div>
      <h2 className="text-xl text-dota-gold mb-3">AI Coach Analysis</h2>
      <div className="grid md:grid-cols-2 gap-4">
        <Card title="Playstyle" accent="gold">
          <p>{analysis.playstyle_summary}</p>
        </Card>
        <Card title="Strength" accent="win">
          <p>{analysis.strength}</p>
        </Card>
        <Card title="Weakness" accent="red">
          <p>{analysis.weakness}</p>
        </Card>
        <Card title="Hero Advice" accent="gold">
          <p>{analysis.hero_advice}</p>
        </Card>
        <div className="md:col-span-2">
          <Card title="Improvements" accent="gold">
            <ul className="space-y-2">
              {analysis.improvements.map((tip, i) => (
                <li key={i} className="flex gap-3">
                  <span className="text-dota-gold font-display font-bold">{i + 1}.</span>
                  <span>{tip}</span>
                </li>
              ))}
            </ul>
          </Card>
        </div>
      </div>
    </div>
  );
}
