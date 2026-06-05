package com.dotalens.dotaanalyszer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchPlayer {
    private Long accountId;
    private String personaName;
    private int heroId;
    private String heroName;
    private int level;
    private int kills;
    private int deaths;
    private int assists;
    private int lastHits;
    private int denies;
    private int gpm;
    private int xpm;
    private int netWorth;
    private int heroDamage;
    private int towerDamage;
    private int heroHealing;
    private List<Integer> items;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getPersonaName() { return personaName; }
    public void setPersonaName(String personaName) { this.personaName = personaName; }
    public int getHeroId() { return heroId; }
    public void setHeroId(int heroId) { this.heroId = heroId; }
    public String getHeroName() { return heroName; }
    public void setHeroName(String heroName) { this.heroName = heroName; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public int getAssists() { return assists; }
    public void setAssists(int assists) { this.assists = assists; }
    public int getLastHits() { return lastHits; }
    public void setLastHits(int lastHits) { this.lastHits = lastHits; }
    public int getDenies() { return denies; }
    public void setDenies(int denies) { this.denies = denies; }
    public int getGpm() { return gpm; }
    public void setGpm(int gpm) { this.gpm = gpm; }
    public int getXpm() { return xpm; }
    public void setXpm(int xpm) { this.xpm = xpm; }
    public int getNetWorth() { return netWorth; }
    public void setNetWorth(int netWorth) { this.netWorth = netWorth; }
    public int getHeroDamage() { return heroDamage; }
    public void setHeroDamage(int heroDamage) { this.heroDamage = heroDamage; }
    public int getTowerDamage() { return towerDamage; }
    public void setTowerDamage(int towerDamage) { this.towerDamage = towerDamage; }
    public int getHeroHealing() { return heroHealing; }
    public void setHeroHealing(int heroHealing) { this.heroHealing = heroHealing; }
    public List<Integer> getItems() { return items; }
    public void setItems(List<Integer> items) { this.items = items; }
}
