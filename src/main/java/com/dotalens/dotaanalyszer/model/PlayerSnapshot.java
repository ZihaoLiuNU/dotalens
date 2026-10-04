package com.dotalens.dotaanalyszer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerSnapshot {
    private long steamId;
    private String personaName;
    private String avatar;
    private String profileUrl;
    private Integer rankTier;
    private Integer leaderboardRank;
    private Integer mmrEstimate;

    private int wins;
    private int losses;
    private double winRate;

    private double avgKills;
    private double avgDeaths;
    private double avgAssists;
    private double avgKda;
    private double avgGpm;
    private double avgXpm;
    private double avgLastHits;

    private List<HeroStat> topHeroes;
    private List<MatchSummary> recentMatches;

    private ModeStats ranked;
    private ModeStats turbo;
    private ModeStats normal;

    // true when served from last-known-good fallback because OpenDota was unavailable
    private boolean stale;

    public long getSteamId() { return steamId; }
    public void setSteamId(long steamId) { this.steamId = steamId; }
    public String getPersonaName() { return personaName; }
    public void setPersonaName(String personaName) { this.personaName = personaName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }
    public Integer getRankTier() { return rankTier; }
    public void setRankTier(Integer rankTier) { this.rankTier = rankTier; }
    public Integer getLeaderboardRank() { return leaderboardRank; }
    public void setLeaderboardRank(Integer leaderboardRank) { this.leaderboardRank = leaderboardRank; }
    public Integer getMmrEstimate() { return mmrEstimate; }
    public void setMmrEstimate(Integer mmrEstimate) { this.mmrEstimate = mmrEstimate; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }
    public double getAvgKills() { return avgKills; }
    public void setAvgKills(double avgKills) { this.avgKills = avgKills; }
    public double getAvgDeaths() { return avgDeaths; }
    public void setAvgDeaths(double avgDeaths) { this.avgDeaths = avgDeaths; }
    public double getAvgAssists() { return avgAssists; }
    public void setAvgAssists(double avgAssists) { this.avgAssists = avgAssists; }
    public double getAvgKda() { return avgKda; }
    public void setAvgKda(double avgKda) { this.avgKda = avgKda; }
    public double getAvgGpm() { return avgGpm; }
    public void setAvgGpm(double avgGpm) { this.avgGpm = avgGpm; }
    public double getAvgXpm() { return avgXpm; }
    public void setAvgXpm(double avgXpm) { this.avgXpm = avgXpm; }
    public double getAvgLastHits() { return avgLastHits; }
    public void setAvgLastHits(double avgLastHits) { this.avgLastHits = avgLastHits; }
    public List<HeroStat> getTopHeroes() { return topHeroes; }
    public void setTopHeroes(List<HeroStat> topHeroes) { this.topHeroes = topHeroes; }
    public List<MatchSummary> getRecentMatches() { return recentMatches; }
    public void setRecentMatches(List<MatchSummary> recentMatches) { this.recentMatches = recentMatches; }
    public ModeStats getRanked() { return ranked; }
    public void setRanked(ModeStats ranked) { this.ranked = ranked; }
    public ModeStats getTurbo() { return turbo; }
    public void setTurbo(ModeStats turbo) { this.turbo = turbo; }
    public ModeStats getNormal() { return normal; }
    public void setNormal(ModeStats normal) { this.normal = normal; }
    public boolean isStale() { return stale; }
    public void setStale(boolean stale) { this.stale = stale; }
}
