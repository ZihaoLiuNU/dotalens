package com.dotalens.dotaanalyszer.model;

public class HeroStat {
    private int heroId;
    private String heroName;
    private int games;
    private int wins;
    private double winRate;

    public HeroStat() {}

    public HeroStat(int heroId, String heroName, int games, int wins, double winRate) {
        this.heroId = heroId;
        this.heroName = heroName;
        this.games = games;
        this.wins = wins;
        this.winRate = winRate;
    }

    public int getHeroId() { return heroId; }
    public void setHeroId(int heroId) { this.heroId = heroId; }
    public String getHeroName() { return heroName; }
    public void setHeroName(String heroName) { this.heroName = heroName; }
    public int getGames() { return games; }
    public void setGames(int games) { this.games = games; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }
}
