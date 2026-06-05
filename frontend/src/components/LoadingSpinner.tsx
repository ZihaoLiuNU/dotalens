export default function LoadingSpinner({ label }: { label?: string }) {
  return (
    <div className="flex flex-col items-center gap-3 py-12">
      <div className="w-12 h-12 border-4 border-dota-border border-t-dota-red rounded-full animate-spin" />
      <div className="text-dota-muted text-sm uppercase tracking-widest font-display">
        {label ?? 'Loading'}
      </div>
    </div>
  );
}
