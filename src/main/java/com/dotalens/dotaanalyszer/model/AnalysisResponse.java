package com.dotalens.dotaanalyszer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResponse {
    private PlayerSnapshot snapshot;
    private AnalysisResult analysis;
    private String analysisError;

    public AnalysisResponse() {}

    public AnalysisResponse(PlayerSnapshot snapshot, AnalysisResult analysis) {
        this.snapshot = snapshot;
        this.analysis = analysis;
    }

    public PlayerSnapshot getSnapshot() { return snapshot; }
    public void setSnapshot(PlayerSnapshot snapshot) { this.snapshot = snapshot; }
    public AnalysisResult getAnalysis() { return analysis; }
    public void setAnalysis(AnalysisResult analysis) { this.analysis = analysis; }
    public String getAnalysisError() { return analysisError; }
    public void setAnalysisError(String analysisError) { this.analysisError = analysisError; }
}
