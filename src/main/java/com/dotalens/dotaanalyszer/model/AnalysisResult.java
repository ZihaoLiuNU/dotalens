package com.dotalens.dotaanalyszer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResult {
    private String weakness;
    private String strength;
    private List<String> improvements;

    @JsonProperty("hero_advice")
    private String heroAdvice;

    @JsonProperty("playstyle_summary")
    private String playstyleSummary;

    public String getWeakness() { return weakness; }
    public void setWeakness(String weakness) { this.weakness = weakness; }
    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }
    public List<String> getImprovements() { return improvements; }
    public void setImprovements(List<String> improvements) { this.improvements = improvements; }
    public String getHeroAdvice() { return heroAdvice; }
    public void setHeroAdvice(String heroAdvice) { this.heroAdvice = heroAdvice; }
    public String getPlaystyleSummary() { return playstyleSummary; }
    public void setPlaystyleSummary(String playstyleSummary) { this.playstyleSummary = playstyleSummary; }
}
