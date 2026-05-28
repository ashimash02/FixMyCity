package com.localissue.service.impl;

import com.localissue.client.ai.AiServiceClient;
import com.localissue.dto.AreaSummaryResponseDto;
import com.localissue.entity.Issue;
import com.localissue.repository.IssueRepository;
import com.localissue.repository.VoteRepository;
import com.localissue.service.AreaSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AreaSummaryServiceImpl implements AreaSummaryService {

    private static final String TRENDING_AREA_LABEL  = "All locations";
    private static final String DEFAULT_AREA_LABEL   = "Selected area";
    private static final String FALLBACK_SUMMARY     = "Summary temporarily unavailable.";
    private static final int    TRENDING_TOP_N       = 4;
    private static final int    SUMMARY_DAYS         = 3;
    /** Round coordinates to 3 decimals (~110 m) so nearby viewers share a cache entry. */
    private static final int    COORD_ROUND_DECIMALS = 3;

    private final IssueRepository issueRepository;
    private final VoteRepository voteRepository;
    private final AiServiceClient aiServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${summary.cache.ttl-minutes:30}")
    private long cacheTtlMinutes;

    @Value("${summary.default-radius-km:10.0}")
    private double defaultRadiusKm;

    @Override
    public AreaSummaryResponseDto getSummary(Double lat, Double lng, Double radiusKm, String areaLabel) {
        boolean trendingMode = (lat == null || lng == null || radiusKm == null);
        return trendingMode
                ? getTrendingSummary()
                : getAreaSummary(lat, lng, radiusKm, areaLabel);
    }

    // ── Trending mode ────────────────────────────────────────────────────────

    private AreaSummaryResponseDto getTrendingSummary() {
        String cached = readCache(TRENDING_CACHE_KEY);
        if (cached != null) {
            return AreaSummaryResponseDto.builder()
                    .area(TRENDING_AREA_LABEL)
                    .summary(cached)
                    .issueCount(TRENDING_TOP_N)
                    .cached(true)
                    .mode(AreaSummaryResponseDto.Mode.TRENDING)
                    .build();
        }

        List<Issue> top = issueRepository
                .findAllOrderByVoteCountDesc(PageRequest.of(0, TRENDING_TOP_N))
                .getContent();

        if (top.isEmpty()) {
            return emptySummary(TRENDING_AREA_LABEL, AreaSummaryResponseDto.Mode.TRENDING);
        }

        List<String> titles = top.stream().map(Issue::getTitle).toList();

        String summary;
        try {
            summary = aiServiceClient.summarise(TRENDING_AREA_LABEL, titles);
        } catch (RestClientException e) {
            return fallbackSummary(TRENDING_AREA_LABEL, titles.size(), AreaSummaryResponseDto.Mode.TRENDING);
        }

        writeCache(TRENDING_CACHE_KEY, summary);

        return AreaSummaryResponseDto.builder()
                .area(TRENDING_AREA_LABEL)
                .summary(summary)
                .issueCount(titles.size())
                .cached(false)
                .mode(AreaSummaryResponseDto.Mode.TRENDING)
                .build();
    }

    // ── Area mode ────────────────────────────────────────────────────────────

    private AreaSummaryResponseDto getAreaSummary(double lat, double lng, double radiusKm, String areaLabel) {
        String label = (areaLabel == null || areaLabel.isBlank()) ? DEFAULT_AREA_LABEL : areaLabel;
        // Always use defaultRadiusKm for the cache key so on-demand reads align with
        // proactive writes from ProactiveSummaryGenerator regardless of what radius
        // the frontend passes.
        String cacheKey = buildAreaCacheKey(lat, lng, defaultRadiusKm);

        String cached = readCache(cacheKey);
        if (cached != null) {
            return AreaSummaryResponseDto.builder()
                    .area(label)
                    .summary(cached)
                    .issueCount(0)
                    .cached(true)
                    .mode(AreaSummaryResponseDto.Mode.AREA)
                    .build();
        }

        List<Issue> nearby = findRecentNearbyIssues(lat, lng, defaultRadiusKm);

        if (nearby.isEmpty()) {
            return emptySummary(label, AreaSummaryResponseDto.Mode.AREA);
        }

        // Sort by vote count desc so the AI sees the most-engaged issues first,
        // then clamp via the client's MAX_ISSUES_PER_REQUEST.
        List<String> titles = nearby.stream()
                .sorted(Comparator.comparingLong(
                        (Issue i) -> voteRepository.countByIssueId(i.getId())).reversed())
                .map(Issue::getTitle)
                .toList();

        String summary;
        try {
            summary = aiServiceClient.summarise(label, titles);
        } catch (RestClientException e) {
            return fallbackSummary(label, nearby.size(), AreaSummaryResponseDto.Mode.AREA);
        }

        writeCache(cacheKey, summary);

        return AreaSummaryResponseDto.builder()
                .area(label)
                .summary(summary)
                .issueCount(nearby.size())
                .cached(false)
                .mode(AreaSummaryResponseDto.Mode.AREA)
                .build();
    }

    // ── Geo filter — date-range aware ────────────────────────────────────────

    /** Returns issues within radiusKm created in the last SUMMARY_DAYS days. */
    private List<Issue> findRecentNearbyIssues(double lat, double lng, double radiusKm) {
        LocalDateTime since = LocalDateTime.now().minusDays(SUMMARY_DAYS);
        double latDelta = radiusKm / 111.32;
        double lngDelta = radiusKm / (111.32 * Math.cos(Math.toRadians(lat)));

        return issueRepository.findWithinBoundingBoxSince(
                        lat - latDelta, lat + latDelta,
                        Math.max(-180, lng - lngDelta), Math.min(180, lng + lngDelta),
                        since)
                .stream()
                .filter(i -> haversineKm(lat, lng, i.getLatitude(), i.getLongitude()) <= radiusKm)
                .toList();
    }

    private static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // ── Cache helpers ────────────────────────────────────────────────────────

    private String buildAreaCacheKey(double lat, double lng, double radiusKm) {
        double factor = Math.pow(10, COORD_ROUND_DECIMALS);
        double rLat = Math.round(lat * factor) / factor;
        double rLng = Math.round(lng * factor) / factor;
        return AREA_CACHE_PREFIX + rLat + ":" + rLng + ":" + radiusKm;
    }

    private String readCache(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value instanceof String s ? s : null;
        } catch (Exception e) {
            log.warn("Redis read failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    private void writeCache(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(cacheTtlMinutes));
        } catch (Exception e) {
            log.warn("Redis write failed for key {}: {}", key, e.getMessage());
        }
    }

    private AreaSummaryResponseDto emptySummary(String label, AreaSummaryResponseDto.Mode mode) {
        return AreaSummaryResponseDto.builder()
                .area(label)
                .summary("No issues reported yet.")
                .issueCount(0)
                .cached(false)
                .mode(mode)
                .build();
    }

    /**
     * Returned when the AI service is unreachable or returns an error.
     * Intentionally NOT cached so the next request retries — transient outage
     * shouldn't be remembered for 30 minutes.
     */
    private AreaSummaryResponseDto fallbackSummary(String label, int issueCount, AreaSummaryResponseDto.Mode mode) {
        return AreaSummaryResponseDto.builder()
                .area(label)
                .summary(FALLBACK_SUMMARY)
                .issueCount(issueCount)
                .cached(false)
                .mode(mode)
                .build();
    }
}
