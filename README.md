# DotaLens

A Dota 2 player performance analyzer powered by [OpenDota](https://www.opendota.com/) and (optionally) [Claude AI](https://www.anthropic.com/claude).

Search any Steam ID to pull a player's profile, recent matches, hero stats, and head-to-head matchup data — broken down by game mode (Ranked / Turbo / Normal) with separate W/L, KDA, GPM/XPM, and top heroes for each bucket.

## Features

- **Steam ID search** — accepts Steam64 (`76561198108791316`) and Steam32 (`148525588`), auto-converts
- **Per-mode breakdown** — All / Ranked / Turbo / Normal tabs, each with its own lifetime W/L, average performance stats, and top heroes
- **Recent 20 matches** — clickable rows open a detail modal showing all 10 players (hero, KDA, last hits/denies, GPM/XPM, net worth, hero/tower/healing damage, items)
- **Hero matchup browser** — see which heroes counter your favorites (filtered to ≥50-game samples for signal)
- **Rank tier + estimated MMR** when OpenDota has it
- **localStorage search history** with avatars for one-click re-lookup
- **Optional AI coach analysis** — when `ANTHROPIC_API_KEY` is set, Claude analyzes the snapshot and returns strengths / weaknesses / improvement tips
- Dark Dota 2 themed UI (Chinese localization)

## Tech Stack

**Backend** — Java 17, Spring Boot, Gradle, Caffeine cache, Spring `RestClient`, Jackson

**Frontend** — React 18, TypeScript, Vite, Tailwind CSS

## Getting Started

### Prerequisites
- JDK 17+
- Node.js 18+
- (Optional) `ANTHROPIC_API_KEY` for the AI analysis feature

### Backend

```bash
./gradlew bootRun
```

Server starts on `http://localhost:8080`. To enable AI analysis:

```powershell
$env:ANTHROPIC_API_KEY = "sk-ant-..."
./gradlew bootRun
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The Vite dev server proxies `/api/*` to the backend, so no CORS config needed.

## API

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/snapshot/{steamId}` | Player snapshot with per-mode breakdown |
| `GET` | `/api/match/{matchId}` | Full match detail (10 players, items, damage breakdown) |
| `GET` | `/api/heroes` | All Dota 2 heroes (id + localized name) |
| `GET` | `/api/heroes/{heroId}/matchups` | Hero matchup data, sorted by disadvantage |
| `GET` | `/api/analyze/{steamId}` | Snapshot + Claude coach analysis (requires API key) |

All endpoints expect Steam32 account IDs. Responses are cached in-memory for 10 minutes via Caffeine.

## Project Structure

```
dotalens/
├── src/main/java/com/dotalens/dotaanalyszer/
│   ├── controller/    REST endpoints
│   ├── service/       OpenDotaService, ClaudeAnalysisService
│   ├── model/         PlayerSnapshot, ModeStats, MatchDetail, HeroMatchup, ...
│   └── config/        CORS, cache, RestClient beans
└── frontend/
    └── src/
        ├── components/   SearchBar, ModeTabs, MatchList, MatchDetailModal,
        │                 HeroList, HeroMatchups, PlayerHeader, StatGrid, ...
        ├── api.ts        Typed API client
        ├── types.ts      TypeScript types mirroring backend models
        ├── searchHistory.ts   localStorage wrapper
        ├── utils.ts      Steam ID normalization, formatting
        └── App.tsx
```

## Notes & Quirks

- The "All" total and the sum of Ranked + Turbo + Normal can differ by a small amount. OpenDota counts a handful of ranked-turbo games in both the Ranked and Turbo buckets — there's no clean filter to deduplicate. The overlap is typically <2%.
- OpenDota's `/wl` and `/heroes` endpoints default to `significant=1`, which silently excludes Turbo. DotaLens passes `significant=0` for the Turbo and "All" buckets to include them.
- Hero matchup lists filter out opponents with fewer than 50 recorded games against the chosen hero, to keep small-sample noise out of the rankings.
- OpenDota's free tier allows 60 requests/minute. Each fresh snapshot performs roughly 11 calls; all responses are cached per-player and per-resource for 10 minutes.
- Win/loss in `/recentMatches` is computed by combining `player_slot` (radiant if < 128) with `radiant_win`. The two need to agree — getting that wrong silently flips half the results.

## Credits

Stats from [OpenDota](https://www.opendota.com/). AI analysis (when enabled) from [Anthropic Claude](https://www.anthropic.com/claude). Not affiliated with Valve or Dota 2.
