package com.dotalens.dotaanalyszer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModeStats {
    private int wins;
    private int losses;
    private double winRate;
    private int recentCount;
    private double avgKills;
    private double avgDeaths;
    private double avgAssists;
    private double avgKda;
    private double avgGpm;
    private double avgXpm;
    private double avgLastHits;
    private List<HeroStat> topHeroes;

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }
    public int getRecentCount() { return recentCount; }
    public void setRecentCount(int recentCount) { this.recentCount = recentCount; }
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
}
