package com.dotalens.dotaanalyszer.model;

public class HeroMatchup {
    private int heroId;
    private String heroName;
    private int gamesPlayed;
    private int wins;
    private double winRate;
    private double disadvantage;

    public HeroMatchup() {}

    public HeroMatchup(int heroId, String heroName, int gamesPlayed, int wins, double winRate, double disadvantage) {
        this.heroId = heroId;
        this.heroName = heroName;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.winRate = winRate;
        this.disadvantage = disadvantage;
    }

    public int getHeroId() { return heroId; }
    public void setHeroId(int heroId) { this.heroId = heroId; }
    public String getHeroName() { return heroName; }
    public void setHeroName(String heroName) { this.heroName = heroName; }
    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }
    public double getDisadvantage() { return disadvantage; }
    public void setDisadvantage(double disadvantage) { this.disadvantage = disadvantage; }
}
