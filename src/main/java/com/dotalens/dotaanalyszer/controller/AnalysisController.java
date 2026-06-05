package com.dotalens.dotaanalyszer.controller;

import com.dotalens.dotaanalyszer.model.AnalysisResponse;
import com.dotalens.dotaanalyszer.model.AnalysisResult;
import com.dotalens.dotaanalyszer.model.HeroMatchup;
import com.dotalens.dotaanalyszer.model.MatchDetail;
import com.dotalens.dotaanalyszer.model.PlayerSnapshot;
import com.dotalens.dotaanalyszer.service.ClaudeAnalysisService;
import com.dotalens.dotaanalyszer.service.OpenDotaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    private final OpenDotaService openDotaService;
    private final ClaudeAnalysisService claudeAnalysisService;

    public AnalysisController(OpenDotaService openDotaService,
                              ClaudeAnalysisService claudeAnalysisService) {
        this.openDotaService = openDotaService;
        this.claudeAnalysisService = claudeAnalysisService;
    }

    @GetMapping("/snapshot/{steamId}")
    public ResponseEntity<?> snapshot(@PathVariable long steamId) {
        try {
            PlayerSnapshot snapshot = openDotaService.buildSnapshot(steamId);
            if (snapshot.getPersonaName() == null && snapshot.getRecentMatches() != null
                    && snapshot.getRecentMatches().isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Player not found or profile is private"
                ));
            }
            return ResponseEntity.ok(snapshot);
        } catch (Exception e) {
            log.error("snapshot failed for {}: {}", steamId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<?> match(@PathVariable long matchId) {
        try {
            MatchDetail md = openDotaService.getMatchDetail(matchId);
            if (md == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Match not found or not yet parsed"));
            }
            return ResponseEntity.ok(md);
        } catch (Exception e) {
            log.error("match {} failed: {}", matchId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/heroes")
    public ResponseEntity<?> heroes() {
        try {
            Map<Integer, String> heroes = openDotaService.getHeroNameMap();
            List<Map<String, Object>> list = heroes.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map(e -> {
                        Map<String, Object> m = new java.util.LinkedHashMap<>();
                        m.put("heroId", e.getKey());
                        m.put("heroName", e.getValue());
                        return m;
                    })
                    .toList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/heroes/{heroId}/matchups")
    public ResponseEntity<?> matchups(@PathVariable int heroId) {
        try {
            List<HeroMatchup> ms = openDotaService.getHeroMatchups(heroId);
            return ResponseEntity.ok(ms);
        } catch (Exception e) {
            log.error("matchups {} failed: {}", heroId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/analyze/{steamId}")
    public ResponseEntity<?> analyze(@PathVariable long steamId) {
        try {
            PlayerSnapshot snapshot = openDotaService.buildSnapshot(steamId);
            if (snapshot.getPersonaName() == null && snapshot.getRecentMatches() != null
                    && snapshot.getRecentMatches().isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Player not found or profile is private"
                ));
            }

            AnalysisResponse resp = new AnalysisResponse();
            resp.setSnapshot(snapshot);
            try {
                AnalysisResult analysis = claudeAnalysisService.analyze(snapshot);
                resp.setAnalysis(analysis);
            } catch (Exception e) {
                log.warn("Claude analysis failed for {}: {}", steamId, e.getMessage());
                resp.setAnalysisError(e.getMessage());
            }
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("analyze failed for {}: {}", steamId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
