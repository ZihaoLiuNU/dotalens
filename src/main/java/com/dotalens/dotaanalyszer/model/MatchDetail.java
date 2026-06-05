package com.dotalens.dotaanalyszer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchDetail {
    private long matchId;
    private int duration;
    private long startTime;
    private boolean radiantWin;
    private int radiantScore;
    private int direScore;
    private String gameMode;
    private String lobbyType;
    private List<MatchPlayer> radiant;
    private List<MatchPlayer> dire;

    public long getMatchId() { return matchId; }
    public void setMatchId(long matchId) { this.matchId = matchId; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public boolean isRadiantWin() { return radiantWin; }
    public void setRadiantWin(boolean radiantWin) { this.radiantWin = radiantWin; }
    public int getRadiantScore() { return radiantScore; }
    public void setRadiantScore(int radiantScore) { this.radiantScore = radiantScore; }
    public int getDireScore() { return direScore; }
    public void setDireScore(int direScore) { this.direScore = direScore; }
    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }
    public String getLobbyType() { return lobbyType; }
    public void setLobbyType(String lobbyType) { this.lobbyType = lobbyType; }
    public List<MatchPlayer> getRadiant() { return radiant; }
    public void setRadiant(List<MatchPlayer> radiant) { this.radiant = radiant; }
    public List<MatchPlayer> getDire() { return dire; }
    public void setDire(List<MatchPlayer> dire) { this.dire = dire; }
}
