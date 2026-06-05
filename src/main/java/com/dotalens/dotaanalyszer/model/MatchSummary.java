package com.dotalens.dotaanalyszer.model;

public class MatchSummary {
    private long matchId;
    private int heroId;
    private String heroName;
    private int kills;
    private int deaths;
    private int assists;
    private int gpm;
    private int xpm;
    private int lastHits;
    private int duration;
    private boolean win;
    private long startTime;
    private String lobbyType;
    private String gameMode;

    public long getMatchId() { return matchId; }
    public void setMatchId(long matchId) { this.matchId = matchId; }
    public int getHeroId() { return heroId; }
    public void setHeroId(int heroId) { this.heroId = heroId; }
    public String getHeroName() { return heroName; }
    public void setHeroName(String heroName) { this.heroName = heroName; }
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public int getAssists() { return assists; }
    public void setAssists(int assists) { this.assists = assists; }
    public int getGpm() { return gpm; }
    public void setGpm(int gpm) { this.gpm = gpm; }
    public int getXpm() { return xpm; }
    public void setXpm(int xpm) { this.xpm = xpm; }
    public int getLastHits() { return lastHits; }
    public void setLastHits(int lastHits) { this.lastHits = lastHits; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public boolean isWin() { return win; }
    public void setWin(boolean win) { this.win = win; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public String getLobbyType() { return lobbyType; }
    public void setLobbyType(String lobbyType) { this.lobbyType = lobbyType; }
    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }
}
