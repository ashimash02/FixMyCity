package com.localissue.client.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

/**
 * Thin HTTP client for the Python AI service (ai-service/).
 *
 * Uses Spring 6's RestClient (sync, fluent). The 10-second timeout is intentional —
 * Gemini calls can take 2–4s under load and we'd rather fail-fast than block the
 * request thread indefinitely.
 */
@Slf4j
@Component
public class AiServiceClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    /** AI service caps issue lists at 50 — clamp here so we never trigger a 422. */
    private static final int MAX_ISSUES_PER_REQUEST = 50;

    private final RestClient http;

    public AiServiceClient(@Value("${ai.service.url}") String baseUrl) {
        this.http = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout((int) TIMEOUT.toMillis());
                    setReadTimeout((int) TIMEOUT.toMillis());
                }})
                .build();
    }

    /**
     * Calls POST /summarise. Returns the AI-generated summary text.
     * Throws RestClientException on failure — callers decide how to handle it.
     */
    public String summarise(String area, List<String> issueTitles) {
        if (issueTitles.size() > MAX_ISSUES_PER_REQUEST) {
            issueTitles = issueTitles.subList(0, MAX_ISSUES_PER_REQUEST);
        }

        SummariseRequest body = new SummariseRequest(area, issueTitles);

        try {
            SummariseResponse response = http.post()
                    .uri("/summarise")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(SummariseResponse.class);

            if (response == null || response.getSummary() == null) {
                throw new RestClientException("AI service returned empty response");
            }
            return response.getSummary();
        } catch (RestClientException e) {
            log.error("AI service /summarise call failed for area '{}': {}", area, e.getMessage());
            throw e;
        }
    }
}
