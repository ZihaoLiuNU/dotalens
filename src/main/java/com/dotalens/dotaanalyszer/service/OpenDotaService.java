package com.dotalens.dotaanalyszer.service;

import com.dotalens.dotaanalyszer.model.HeroMatchup;
import com.dotalens.dotaanalyszer.model.HeroStat;
import com.dotalens.dotaanalyszer.model.MatchDetail;
import com.dotalens.dotaanalyszer.model.MatchPlayer;
import com.dotalens.dotaanalyszer.model.MatchSummary;
import com.dotalens.dotaanalyszer.model.ModeStats;
import com.dotalens.dotaanalyszer.model.PlayerSnapshot;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OpenDotaService {

    private static final Logger log = LoggerFactory.getLogger(OpenDotaService.class);

    private final RestClient restClient;
    private final String baseUrl;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    // Last-known-good snapshot per player, used to degrade gracefully when OpenDota is down.
    private final Map<Long, PlayerSnapshot> lastGoodSnapshot = new ConcurrentHashMap<>();

    // buildSnapshot() fires ~11 independent OpenDota GETs; run them concurrently instead of one
    // at a time so total wall-clock time is bounded by the slowest call, not their sum.
    private final ExecutorService executor = Executors.newFixedThreadPool(12,
            r -> { Thread t = new Thread(r, "opendota-fetch"); t.setDaemon(true); return t; });

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

    private static final Map<Integer, String> LOBBY_TYPES = Map.ofEntries(
            Map.entry(-1, "Invalid"), Map.entry(0, "Normal"), Map.entry(1, "Practice"),
            Map.entry(2, "Tournament"), Map.entry(3, "Tutorial"), Map.entry(4, "Co-op vs Bots"),
            Map.entry(5, "Team Match"), Map.entry(6, "Solo Queue"), Map.entry(7, "Ranked"),
            Map.entry(8, "1v1 Mid"), Map.entry(9, "Battle Cup")
    );

    private static final Map<Integer, String> GAME_MODES = Map.ofEntries(
            Map.entry(0, "Unknown"), Map.entry(1, "All Pick"), Map.entry(2, "Captains Mode"),
            Map.entry(3, "Random Draft"), Map.entry(4, "Single Draft"), Map.entry(5, "All Random"),
            Map.entry(8, "Reverse CM"), Map.entry(16, "Captains Draft"), Map.entry(18, "All Draft"),
            Map.entry(22, "All Draft Ranked"), Map.entry(23, "Turbo")
    );

    private enum ModeKey {
        RANKED("lobby_type=7", ms -> "Ranked".equals(ms.getLobbyType()) && !"Turbo".equals(ms.getGameMode())),
        TURBO("significant=0&game_mode=23", ms -> "Turbo".equals(ms.getGameMode())),
        NORMAL("lobby_type=0", ms -> !"Ranked".equals(ms.getLobbyType()) && !"Turbo".equals(ms.getGameMode()));

        final String filter;
        final Predicate<MatchSummary> matcher;

        ModeKey(String filter, Predicate<MatchSummary> matcher) {
            this.filter = filter;
            this.matcher = matcher;
        }
    }

    public OpenDotaService(RestClient restClient,
                           @Value("${opendota.base-url}") String baseUrl,
                           CircuitBreaker openDotaCircuitBreaker,
                           Retry openDotaRetry) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
        this.circuitBreaker = openDotaCircuitBreaker;
        this.retry = openDotaRetry;
    }

    // Don't cache a stale fallback as if it were fresh — it must be re-attempted once OpenDota recovers.
    @Cacheable(value = "snapshot", key = "#steamId", unless = "#result != null && #result.stale")
    public PlayerSnapshot buildSnapshot(long steamId) {
        // Fail fast: if the circuit is already OPEN, don't fire ~8 doomed calls — serve last-known-good.
        if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            PlayerSnapshot stale = lastGoodSnapshot.get(steamId);
            if (stale != null) {
                log.warn("OpenDota circuit OPEN — serving STALE snapshot for steamId={}", steamId);
                stale.setStale(true);
                return stale;
            }
            throw new IllegalStateException("OpenDota is temporarily unavailable, please retry shortly");
        }

        log.info("Building snapshot for steamId={}", steamId);

        // Every GET below is independent of the others, so fan them all out at once instead of
        // waiting on them one by one — total time becomes ~the slowest call, not their sum.
        CompletableFuture<Map<String, Object>> profileFut =
                CompletableFuture.supplyAsync(() -> getJson("/players/" + steamId), executor);
        CompletableFuture<Map<String, Object>> wlFut =
                CompletableFuture.supplyAsync(() -> getJson("/players/" + steamId + "/wl?significant=0"), executor);
        CompletableFuture<List<Map<String, Object>>> recentFut =
                CompletableFuture.supplyAsync(() -> getJsonList("/players/" + steamId + "/recentMatches"), executor);
        CompletableFuture<List<Map<String, Object>>> heroesFut =
                CompletableFuture.supplyAsync(() -> getJsonList("/players/" + steamId + "/heroes?significant=0"), executor);
        CompletableFuture<Map<Integer, String>> heroNameFut =
                CompletableFuture.supplyAsync(this::getHeroNameMap, executor);

        Map<ModeKey, CompletableFuture<Map<String, Object>>> modeWlFuts = new EnumMap<>(ModeKey.class);
        Map<ModeKey, CompletableFuture<List<Map<String, Object>>>> modeHeroesFuts = new EnumMap<>(ModeKey.class);
        for (ModeKey mode : ModeKey.values()) {
            modeWlFuts.put(mode, CompletableFuture.supplyAsync(
                    () -> getJson("/players/" + steamId + "/wl?" + mode.filter), executor));
            modeHeroesFuts.put(mode, CompletableFuture.supplyAsync(
                    () -> getJsonList("/players/" + steamId + "/heroes?" + mode.filter), executor));
        }

        CompletableFuture.allOf(Stream.concat(
                Stream.of(profileFut, wlFut, recentFut, heroesFut, heroNameFut),
                Stream.concat(modeWlFuts.values().stream(), modeHeroesFuts.values().stream())
        ).toArray(CompletableFuture[]::new)).join();

        Map<String, Object> profileResp = profileFut.join();
        Map<String, Object> wl = wlFut.join();
        List<Map<String, Object>> recent = recentFut.join();
        List<Map<String, Object>> heroes = heroesFut.join();
        Map<Integer, String> heroIdToName = heroNameFut.join();

        PlayerSnapshot s = new PlayerSnapshot();
        s.setSteamId(steamId);

        Map<String, Object> profile = asMap(profileResp.get("profile"));
        if (profile != null) {
            s.setPersonaName(str(profile.get("personaname")));
            s.setAvatar(str(profile.get("avatarfull")));
            s.setProfileUrl(str(profile.get("profileurl")));
        }
        s.setRankTier(asInt(profileResp.get("rank_tier")));
        s.setLeaderboardRank(asInt(profileResp.get("leaderboard_rank")));
        Map<String, Object> mmr = asMap(profileResp.get("mmr_estimate"));
        if (mmr != null) s.setMmrEstimate(asInt(mmr.get("estimate")));

        int wins = asInt(wl.get("win"), 0);
        int losses = asInt(wl.get("lose"), 0);
        s.setWins(wins);
        s.setLosses(losses);
        int total = wins + losses;
        s.setWinRate(total == 0 ? 0.0 : round2((double) wins / total * 100.0));

        // recent matches aggregation
        List<MatchSummary> summaries = new ArrayList<>();
        long sumK = 0, sumD = 0, sumA = 0, sumGpm = 0, sumXpm = 0, sumLh = 0;
        for (Map<String, Object> m : recent) {
            MatchSummary ms = new MatchSummary();
            ms.setMatchId(asLong(m.get("match_id"), 0));
            int heroId = asInt(m.get("hero_id"), 0);
            ms.setHeroId(heroId);
            ms.setHeroName(heroIdToName.getOrDefault(heroId, "Hero " + heroId));
            ms.setKills(asInt(m.get("kills"), 0));
            ms.setDeaths(asInt(m.get("deaths"), 0));
            ms.setAssists(asInt(m.get("assists"), 0));
            ms.setGpm(asInt(m.get("gold_per_min"), 0));
            ms.setXpm(asInt(m.get("xp_per_min"), 0));
            ms.setLastHits(asInt(m.get("last_hits"), 0));
            ms.setDuration(asInt(m.get("duration"), 0));
            ms.setStartTime(asLong(m.get("start_time"), 0));
            int playerSlot = asInt(m.get("player_slot"), 0);
            boolean radiantWin = asBool(m.get("radiant_win"));
            boolean radiantPlayer = playerSlot < 128;
            ms.setWin(radiantPlayer == radiantWin);
            ms.setLobbyType(LOBBY_TYPES.getOrDefault(asInt(m.get("lobby_type"), -2), "Unknown"));
            ms.setGameMode(GAME_MODES.getOrDefault(asInt(m.get("game_mode"), -1), "Unknown"));

            sumK += ms.getKills();
            sumD += ms.getDeaths();
            sumA += ms.getAssists();
            sumGpm += ms.getGpm();
            sumXpm += ms.getXpm();
            sumLh += ms.getLastHits();
            summaries.add(ms);
        }
        int n = Math.max(summaries.size(), 1);
        s.setAvgKills(round2((double) sumK / n));
        s.setAvgDeaths(round2((double) sumD / n));
        s.setAvgAssists(round2((double) sumA / n));
        double avgDeathsSafe = sumD == 0 ? 1 : (double) sumD / n;
        s.setAvgKda(round2(((double) sumK + sumA) / n / avgDeathsSafe));
        s.setAvgGpm(round2((double) sumGpm / n));
        s.setAvgXpm(round2((double) sumXpm / n));
        s.setAvgLastHits(round2((double) sumLh / n));
        s.setRecentMatches(summaries);

        // top heroes (most played, then map names + winrate)
        List<HeroStat> topHeroes = heroes.stream()
                .sorted(Comparator.comparingInt((Map<String, Object> h) -> asInt(h.get("games"), 0)).reversed())
                .limit(8)
                .map(h -> {
                    int hid = asInt(h.get("hero_id"), 0);
                    int games = asInt(h.get("games"), 0);
                    int hWins = asInt(h.get("win"), 0);
                    double wr = games == 0 ? 0.0 : round2((double) hWins / games * 100.0);
                    return new HeroStat(hid, heroIdToName.getOrDefault(hid, "Hero " + hid), games, hWins, wr);
                })
                .collect(Collectors.toList());
        s.setTopHeroes(topHeroes);

        // per-mode breakdowns — the wl/heroes calls for each mode were already fetched above
        for (ModeKey mode : ModeKey.values()) {
            List<MatchSummary> recentInMode = summaries.stream()
                    .filter(mode.matcher)
                    .collect(Collectors.toList());
            ModeStats stats = buildModeStats(modeWlFuts.get(mode).join(), modeHeroesFuts.get(mode).join(),
                    recentInMode, heroIdToName);
            switch (mode) {
                case RANKED -> s.setRanked(stats);
                case TURBO -> s.setTurbo(stats);
                case NORMAL -> s.setNormal(stats);
            }
        }

        // Remember the last healthy result so we can degrade gracefully if OpenDota later goes down.
        boolean healthy = s.getPersonaName() != null
                || (s.getRecentMatches() != null && !s.getRecentMatches().isEmpty());
        if (healthy) {
            s.setStale(false);
            lastGoodSnapshot.put(steamId, s);
        }

        return s;
    }

    private ModeStats buildModeStats(Map<String, Object> wl, List<Map<String, Object>> heroes,
                                     List<MatchSummary> recentInMode, Map<Integer, String> heroIdToName) {
        ModeStats m = new ModeStats();

        int wins = asInt(wl.get("win"), 0);
        int losses = asInt(wl.get("lose"), 0);
        m.setWins(wins);
        m.setLosses(losses);
        int total = wins + losses;
        m.setWinRate(total == 0 ? 0.0 : round2((double) wins / total * 100.0));

        int n = recentInMode.size();
        m.setRecentCount(n);
        if (n > 0) {
            long sumK = 0, sumD = 0, sumA = 0, sumGpm = 0, sumXpm = 0, sumLh = 0;
            for (MatchSummary ms : recentInMode) {
                sumK += ms.getKills();
                sumD += ms.getDeaths();
                sumA += ms.getAssists();
                sumGpm += ms.getGpm();
                sumXpm += ms.getXpm();
                sumLh += ms.getLastHits();
            }
            m.setAvgKills(round2((double) sumK / n));
            m.setAvgDeaths(round2((double) sumD / n));
            m.setAvgAssists(round2((double) sumA / n));
            double avgD = sumD == 0 ? 1 : (double) sumD / n;
            m.setAvgKda(round2(((double) sumK + sumA) / n / avgD));
            m.setAvgGpm(round2((double) sumGpm / n));
            m.setAvgXpm(round2((double) sumXpm / n));
            m.setAvgLastHits(round2((double) sumLh / n));
        }

        List<HeroStat> topHeroes = heroes.stream()
                .sorted(Comparator.comparingInt((Map<String, Object> h) -> asInt(h.get("games"), 0)).reversed())
                .filter(h -> asInt(h.get("games"), 0) > 0)
                .limit(8)
                .map(h -> {
                    int hid = asInt(h.get("hero_id"), 0);
                    int games = asInt(h.get("games"), 0);
                    int hWins = asInt(h.get("win"), 0);
                    double wr = games == 0 ? 0.0 : round2((double) hWins / games * 100.0);
                    return new HeroStat(hid, heroIdToName.getOrDefault(hid, "Hero " + hid), games, hWins, wr);
                })
                .collect(Collectors.toList());
        m.setTopHeroes(topHeroes);
        return m;
    }

    @Cacheable(value = "match", key = "#matchId")
    public MatchDetail getMatchDetail(long matchId) {
        Map<String, Object> raw = getJson("/matches/" + matchId);
        if (raw == null || raw.isEmpty() || raw.get("match_id") == null) {
            return null;
        }
        Map<Integer, String> heroIdToName = getHeroNameMap();

        MatchDetail md = new MatchDetail();
        md.setMatchId(asLong(raw.get("match_id"), matchId));
        md.setDuration(asInt(raw.get("duration"), 0));
        md.setStartTime(asLong(raw.get("start_time"), 0));
        md.setRadiantWin(Boolean.TRUE.equals(raw.get("radiant_win")));
        md.setRadiantScore(asInt(raw.get("radiant_score"), 0));
        md.setDireScore(asInt(raw.get("dire_score"), 0));
        md.setGameMode(GAME_MODES.getOrDefault(asInt(raw.get("game_mode"), -1), "Unknown"));
        md.setLobbyType(LOBBY_TYPES.getOrDefault(asInt(raw.get("lobby_type"), -2), "Unknown"));

        Object playersObj = raw.get("players");
        List<MatchPlayer> radiant = new ArrayList<>();
        List<MatchPlayer> dire = new ArrayList<>();
        if (playersObj instanceof List<?> players) {
            for (Object p : players) {
                if (!(p instanceof Map<?, ?> pm)) continue;
                @SuppressWarnings("unchecked")
                Map<String, Object> pmap = (Map<String, Object>) pm;
                MatchPlayer mp = new MatchPlayer();
                mp.setAccountId(pmap.get("account_id") == null ? null : asLong(pmap.get("account_id"), 0));
                mp.setPersonaName(str(pmap.get("personaname")));
                int hid = asInt(pmap.get("hero_id"), 0);
                mp.setHeroId(hid);
                mp.setHeroName(heroIdToName.getOrDefault(hid, "Hero " + hid));
                mp.setLevel(asInt(pmap.get("level"), 0));
                mp.setKills(asInt(pmap.get("kills"), 0));
                mp.setDeaths(asInt(pmap.get("deaths"), 0));
                mp.setAssists(asInt(pmap.get("assists"), 0));
                mp.setLastHits(asInt(pmap.get("last_hits"), 0));
                mp.setDenies(asInt(pmap.get("denies"), 0));
                mp.setGpm(asInt(pmap.get("gold_per_min"), 0));
                mp.setXpm(asInt(pmap.get("xp_per_min"), 0));
                mp.setNetWorth(asInt(pmap.get("net_worth"), 0));
                mp.setHeroDamage(asInt(pmap.get("hero_damage"), 0));
                mp.setTowerDamage(asInt(pmap.get("tower_damage"), 0));
                mp.setHeroHealing(asInt(pmap.get("hero_healing"), 0));
                List<Integer> items = new ArrayList<>();
                for (int i = 0; i <= 5; i++) items.add(asInt(pmap.get("item_" + i), 0));
                mp.setItems(items);

                int slot = asInt(pmap.get("player_slot"), 0);
                if (slot < 128) radiant.add(mp); else dire.add(mp);
            }
        }
        md.setRadiant(radiant);
        md.setDire(dire);
        return md;
    }

    @Cacheable(value = "matchups", key = "#heroId")
    public List<HeroMatchup> getHeroMatchups(int heroId) {
        List<Map<String, Object>> raw = getJsonList("/heroes/" + heroId + "/matchups");
        Map<Integer, String> heroIdToName = getHeroNameMap();
        return raw.stream()
                .map(m -> {
                    int hid = asInt(m.get("hero_id"), 0);
                    int games = asInt(m.get("games_played"), 0);
                    int wins = asInt(m.get("wins"), 0);
                    double wr = games == 0 ? 0.0 : round2((double) wins / games * 100.0);
                    // disadvantage = 50 - winRate of this hero vs us (i.e. how much they hurt us)
                    double disadvantage = round2(50.0 - wr);
                    return new HeroMatchup(hid, heroIdToName.getOrDefault(hid, "Hero " + hid),
                            games, wins, wr, disadvantage);
                })
                .filter(h -> h.getGamesPlayed() >= 50)
                .sorted(Comparator.comparingDouble(HeroMatchup::getDisadvantage).reversed())
                .collect(Collectors.toList());
    }

    @Cacheable(value = "heroes", key = "'all'")
    public Map<Integer, String> getHeroNameMap() {
        List<Map<String, Object>> heroes = getJsonList("/heroes");
        Map<Integer, String> map = new HashMap<>();
        for (Map<String, Object> h : heroes) {
            Integer id = asInt(h.get("id"));
            String name = str(h.get("localized_name"));
            if (id != null && name != null) map.put(id, name);
        }
        return map;
    }

    private Map<String, Object> getJson(String path) {
        try {
            Map<String, Object> body = callOpenDota(path, () -> restClient.get()
                    .uri(baseUrl + path)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {}));
            return body == null ? Map.of() : body;
        } catch (CallNotPermittedException e) {
            log.warn("OpenDota circuit OPEN — skipping GET {} (fail fast)", path);
            return Map.of();
        } catch (Exception e) {
            log.warn("OpenDota GET {} failed after retries: {}", path, e.getMessage());
            return Map.of();
        }
    }

    private List<Map<String, Object>> getJsonList(String path) {
        try {
            List<Map<String, Object>> body = callOpenDota(path, () -> restClient.get()
                    .uri(baseUrl + path)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {}));
            return body == null ? List.of() : body;
        } catch (CallNotPermittedException e) {
            log.warn("OpenDota circuit OPEN — skipping GET {} (fail fast)", path);
            return List.of();
        } catch (Exception e) {
            log.warn("OpenDota GET {} failed after retries: {}", path, e.getMessage());
            return List.of();
        }
    }

    /**
     * Wrap one OpenDota HTTP call with resilience at the external boundary:
     * CircuitBreaker(Retry(call)). Retry smooths over transient blips; the circuit breaker
     * trips after a sustained failure/slow rate and then fails fast (CallNotPermittedException)
     * so we neither hammer a struggling dependency nor pile up blocked threads.
     */
    private <T> T callOpenDota(String path, Supplier<T> call) {
        Supplier<T> withRetry = Retry.decorateSupplier(retry, call);
        Supplier<T> withCircuitBreaker = CircuitBreaker.decorateSupplier(circuitBreaker, withRetry);
        return withCircuitBreaker.get();
    }

    private static Map<String, Object> asMap(Object o) {
        return o instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
    }

    private static String str(Object o) { return o == null ? null : o.toString(); }

    private static Integer asInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (NumberFormatException e) { return null; }
    }

    private static int asInt(Object o, int dflt) {
        Integer v = asInt(o);
        return v == null ? dflt : v;
    }

    private static boolean asBool(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() != 0;
        if (o == null) return false;
        String s = o.toString().toLowerCase();
        return s.equals("true") || s.equals("1");
    }

    private static long asLong(Object o, long dflt) {
        if (o == null) return dflt;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(o.toString()); } catch (NumberFormatException e) { return dflt; }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
