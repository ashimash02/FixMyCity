package com.localissue.rabbitmq.consumer;

import com.localissue.client.ai.AiServiceClient;
import com.localissue.config.RabbitMQConfig;
import com.localissue.entity.Issue;
import com.localissue.rabbitmq.events.IssueEvent;
import com.localissue.repository.IssueRepository;
import com.localissue.repository.VoteRepository;
import com.localissue.service.AreaSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Consumes issue lifecycle events and proactively regenerates the AI summary
 * for the affected area (last 3 days, within the configured radius).
 *
 * This replaces the old SummaryCacheInvalidator wipe-all approach. Instead of
 * deleting the cache and waiting for a user request to trigger regeneration,
 * we generate the new summary immediately so the next user always gets a
 * pre-computed, up-to-date result.
 *
 * Retry behaviour: 3 attempts with exponential backoff via RabbitMQConfig.
 * On exhaustion the message goes to the DLQ — the on-demand fallback in
 * AreaSummaryServiceImpl will cover users until the next event regenerates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProactiveSummaryGenerator {

    private static final int    SUMMARY_DAYS      = 3;
    private static final int    MAX_ISSUES_TO_AI  = 50;

    private final IssueRepository issueRepository;
    private final VoteRepository voteRepository;
    private final AiServiceClient aiServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${summary.default-radius-km:10.0}")
    private double defaultRadiusKm;

    @Value("${summary.cache.ttl-minutes:30}")
    private long cacheTtlMinutes;

    @RabbitListener(queues = RabbitMQConfig.AI_SUMMARY_QUEUE)
    public void onIssueEvent(IssueEvent event) {
        log.info("Proactive summary triggered by {} for issue {} (area='{}', lat={}, lng={})",
                event.getEventType(), event.getIssueId(),
                event.getLocationName(), event.getLatitude(), event.getLongitude());

        // Skip if the issue has no coordinates — cannot do geo-based generation
        if (event.getLatitude() == null || event.getLongitude() == null) {
            log.warn("Issue {} has no coordinates — skipping proactive summary", event.getIssueId());
            invalidateTrending();
            return;
        }

        regenerateAreaSummary(event.getLatitude(), event.getLongitude(), event.getLocationName());
        invalidateTrending();
    }

    // ── Area summary ─────────────────────────────────────────────────────────

    private void regenerateAreaSummary(double lat, double lng, String locationName) {
        List<Issue> recent = findRecentNearby(lat, lng);

        if (recent.isEmpty()) {
            log.debug("No recent issues near ({}, {}) — clearing area summary cache", lat, lng);
            redisTemplate.delete(buildAreaCacheKey(lat, lng));
            return;
        }

        List<String> titles = recent.stream()
                .sorted(Comparator.comparingLong(
                        (Issue i) -> voteRepository.countByIssueId(i.getId())).reversed())
                .limit(MAX_ISSUES_TO_AI)
                .map(Issue::getTitle)
                .toList();

        String areaLabel = shortName(locationName);

        try {
            String summary = aiServiceClient.summarise(areaLabel, titles);
            String cacheKey = buildAreaCacheKey(lat, lng);
            redisTemplate.opsForValue().set(cacheKey, summary, Duration.ofMinutes(cacheTtlMinutes));
            log.info("Area summary stored at key '{}' ({} issues, area='{}')",
                    cacheKey, titles.size(), areaLabel);
        } catch (Exception e) {
            // Don't rethrow — a failed AI call should not send the event to the DLQ.
            // The on-demand fallback will cover users until the next event triggers a retry.
            log.error("AI call failed during proactive generation for area '{}': {}", areaLabel, e.getMessage());
        }
    }

    // ── Trending invalidation ─────────────────────────────────────────────────
    // Trending is based on all-time vote counts and is cheap to regenerate on-demand,
    // so we just invalidate it here rather than proactively regenerating.

    private void invalidateTrending() {
        try {
            redisTemplate.delete(AreaSummaryService.TRENDING_CACHE_KEY);
        } catch (Exception e) {
            log.warn("Failed to invalidate trending cache: {}", e.getMessage());
        }
    }

    // ── Geo helpers ───────────────────────────────────────────────────────────

    private List<Issue> findRecentNearby(double lat, double lng) {
        LocalDateTime since = LocalDateTime.now().minusDays(SUMMARY_DAYS);
        double latDelta = defaultRadiusKm / 111.32;
        double lngDelta = defaultRadiusKm / (111.32 * Math.cos(Math.toRadians(lat)));

        return issueRepository.findWithinBoundingBoxSince(
                        lat - latDelta, lat + latDelta,
                        Math.max(-180, lng - lngDelta), Math.min(180, lng + lngDelta),
                        since)
                .stream()
                .filter(i -> haversineKm(lat, lng, i.getLatitude(), i.getLongitude()) <= defaultRadiusKm)
                .toList();
    }

    private static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private String buildAreaCacheKey(double lat, double lng) {
        double factor = Math.pow(10, 3);
        double rLat = Math.round(lat * factor) / factor;
        double rLng = Math.round(lng * factor) / factor;
        return AreaSummaryService.AREA_CACHE_PREFIX + rLat + ":" + rLng + ":" + defaultRadiusKm;
    }

    /** Mirrors the shortName() function in the frontend's NavLocationPicker.jsx. */
    private static String shortName(String locationName) {
        if (locationName == null || locationName.isBlank()) return "Selected area";
        return locationName.split(",")[0].trim();
    }
}
