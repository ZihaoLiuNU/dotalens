package com.dotalens.dotaanalyszer.service;

import com.dotalens.dotaanalyszer.model.AnalysisResult;
import com.dotalens.dotaanalyszer.model.PlayerSnapshot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class ClaudeAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeAnalysisService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String model;
    private final String apiKey;

    public ClaudeAnalysisService(RestClient restClient,
                                 ObjectMapper objectMapper,
                                 @Value("${claude.base-url}") String baseUrl,
                                 @Value("${claude.model}") String model,
                                 @Value("${claude.api-key}") String apiKey) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.model = model;
        this.apiKey = apiKey;
    }

    @Cacheable(value = "analysis", key = "#snapshot.steamId")
    public AnalysisResult analyze(PlayerSnapshot snapshot) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY environment variable is not set");
        }

        String snapshotJson;
        try {
            snapshotJson = objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize snapshot", e);
        }

        String prompt = """
                You are a Dota 2 coach analyzing a player's performance based on OpenDota stats.

                Here is the player snapshot (JSON):
                %s

                Analyze this player's strengths, weaknesses, and provide actionable improvement advice.
                Consider their KDA, GPM, XPM, last hits, winrate, hero pool, and recent match results.

                Respond ONLY with valid JSON in EXACTLY this shape (no markdown, no commentary outside JSON):
                {
                  "weakness": "one concise sentence describing the player's main weakness",
                  "strength": "one concise sentence describing the player's main strength",
                  "improvements": ["actionable tip 1", "actionable tip 2", "actionable tip 3"],
                  "hero_advice": "1-2 sentences on hero pool / picks recommendations",
                  "playstyle_summary": "1-2 sentences summarizing the player's overall playstyle"
                }
                """.formatted(snapshotJson);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 1024,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        Map<String, Object> response = restClient.post()
                .uri(baseUrl)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        String text = extractText(response);
        if (text == null || text.isBlank()) {
            throw new RuntimeException("Claude returned empty response");
        }

        String json = extractJson(text);
        try {
            return objectMapper.readValue(json, AnalysisResult.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse Claude JSON. Raw: {}", text);
            throw new RuntimeException("Claude did not return valid JSON: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        if (response == null) return null;
        Object content = response.get("content");
        if (!(content instanceof List<?> blocks) || blocks.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (Object block : blocks) {
            if (block instanceof Map<?, ?> m && "text".equals(m.get("type"))) {
                Object t = m.get("text");
                if (t != null) sb.append(t);
            }
        }
        return sb.toString();
    }

    private String extractJson(String text) {
        String t = text.trim();
        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            if (firstNewline > 0) t = t.substring(firstNewline + 1);
            int closeFence = t.lastIndexOf("```");
            if (closeFence >= 0) t = t.substring(0, closeFence);
            t = t.trim();
        }
        int start = t.indexOf('{');
        int end = t.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return t.substring(start, end + 1);
        }
        return t;
    }
}
