package com.localissue.service.impl;

import com.localissue.dto.IssueEditDto;
import com.localissue.dto.IssueRequestDto;
import com.localissue.dto.IssueResponseDto;
import com.localissue.dto.IssueStatusUpdateDto;
import com.localissue.dto.LocationFilter;
import com.localissue.entity.Issue;
import com.localissue.exception.ResourceNotFoundException;
import com.localissue.repository.FollowRepository;
import com.localissue.repository.IssueRepository;
import com.localissue.repository.VoteRepository;
import com.localissue.service.IssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private static final Set<String> VALID_STATUSES = Set.of(
            "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"
    );
    private static final String TRENDING_CACHE_PREFIX = "trending:page:";
    private static final Duration TRENDING_TTL = Duration.ofMinutes(10);

    private final IssueRepository issueRepository;
    private final VoteRepository voteRepository;
    private final FollowRepository followRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public IssueResponseDto createIssue(IssueRequestDto requestDto, String userId, String username) {
        Issue issue = Issue.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .latitude(requestDto.getLatitude())
                .longitude(requestDto.getLongitude())
                .locationName(requestDto.getLocationName())
                .category(requestDto.getCategory())
                .imageUrl(requestDto.getImageUrl())
                .status("OPEN")
                .createdBy(userId)
                .createdByUsername(username)
                .build();

        IssueResponseDto result = mapToResponse(issueRepository.save(issue), userId);
        evictTrendingCache();
        return result;
    }

    @Override
    public Page<IssueResponseDto> getAllIssues(Pageable pageable, String requestingUserId, LocationFilter location) {
        if (location == null) {
            return issueRepository.findAll(pageable)
                    .map(issue -> mapToResponse(issue, requestingUserId, null));
        }
        // Bounding box pre-filter, then precise Haversine, then sort nearest first
        List<IssueResponseDto> results = candidatesWithDistance(location).stream()
                .sorted(Comparator.comparingDouble(IssueDist::distanceKm))
                .map(d -> mapToResponse(d.issue(), requestingUserId, d.distanceKm()))
                .toList();
        return toPage(results, pageable);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<IssueResponseDto> getTrendingIssues(Pageable pageable, String requestingUserId, LocationFilter location) {
        if (location != null) {
            List<IssueResponseDto> results = candidatesWithDistance(location).stream()
                    .map(d -> mapToResponse(d.issue(), requestingUserId, d.distanceKm()))
                    .sorted(Comparator.comparingLong(IssueResponseDto::getVoteCount).reversed())
                    .toList();
            return toPage(results, pageable);
        }

        String cacheKey = TRENDING_CACHE_PREFIX + pageable.getPageNumber() + ":" + pageable.getPageSize();
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof List<?> cachedList) {
                List<IssueResponseDto> dtos = (List<IssueResponseDto>) cachedList;
                dtos = hydrateHasVoted(dtos, requestingUserId);
                return new PageImpl<>(dtos, pageable, dtos.size());
            }
        } catch (Exception e) {
            log.warn("Redis read failed for key {}: {}", cacheKey, e.getMessage());
        }

        Page<IssueResponseDto> page = issueRepository.findAllOrderByVoteCountDesc(pageable)
                .map(issue -> mapToResponse(issue, null, null));

        try {
            redisTemplate.opsForValue().set(cacheKey, page.getContent(), TRENDING_TTL);
        } catch (Exception e) {
            log.warn("Redis write failed for key {}: {}", cacheKey, e.getMessage());
        }

        List<IssueResponseDto> hydrated = hydrateHasVoted(page.getContent(), requestingUserId);
        return new PageImpl<>(hydrated, pageable, page.getTotalElements());
    }

    @Override
    public IssueResponseDto getIssueById(Long id, String requestingUserId) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));
        return mapToResponse(issue, requestingUserId);
    }

    @Override
    public Page<IssueResponseDto> getMyIssues(Pageable pageable, String userId) {
        return issueRepository.findByCreatedBy(userId, pageable)
                .map(issue -> mapToResponse(issue, userId));
    }

    @Override
    public IssueResponseDto editIssue(Long id, IssueEditDto dto, String requestingUserId) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));

        if (!issue.getCreatedBy().equals(requestingUserId)) {
            throw new SecurityException("You are not allowed to edit this issue");
        }

        issue.setTitle(dto.getTitle());
        issue.setDescription(dto.getDescription());
        issue.setLocationName(dto.getLocationName());
        issue.setLatitude(dto.getLatitude());
        issue.setLongitude(dto.getLongitude());
        issue.setCategory(dto.getCategory());
        issue.setImageUrl(dto.getImageUrl());

        IssueResponseDto result = mapToResponse(issueRepository.save(issue), requestingUserId);
        evictTrendingCache();
        return result;
    }

    @Override
    public Page<IssueResponseDto> getFollowingFeed(Pageable pageable, String userId) {
        List<String> followingIds = followRepository.findFollowingIdsByFollowerId(userId);
        if (followingIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return issueRepository.findByCreatedByIn(followingIds, pageable)
                .map(issue -> mapToResponse(issue, userId));
    }

    @Override
    public void deleteIssue(Long id, String requestingUserId) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));

        if (!issue.getCreatedBy().equals(requestingUserId)) {
            throw new SecurityException("You are not allowed to delete this issue");
        }

        issueRepository.delete(issue);
        evictTrendingCache();
    }

    @Override
    public IssueResponseDto updateIssueStatus(Long id, IssueStatusUpdateDto dto) {
        String newStatus = dto.getStatus().toUpperCase();
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status '" + dto.getStatus() + "'. Allowed values: " + VALID_STATUSES
            );
        }

        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));

        issue.setStatus(newStatus);
        return mapToResponse(issueRepository.save(issue), null);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private record IssueDist(Issue issue, double distanceKm) {}

    /** Bounding box pre-filter + precise Haversine → sorted candidates with distance. */
    private List<IssueDist> candidatesWithDistance(LocationFilter filter) {
        double lat = filter.latitude();
        double lng = filter.longitude();
        double radius = filter.radiusKm();

        // 1 degree latitude ≈ 111.32 km; longitude degree shrinks with cos(lat)
        double latDelta = radius / 111.32;
        double lngDelta = radius / (111.32 * Math.cos(Math.toRadians(lat)));

        return issueRepository.findWithinBoundingBox(
                        lat - latDelta, lat + latDelta,
                        Math.max(-180, lng - lngDelta), Math.min(180, lng + lngDelta))
                .stream()
                .map(issue -> new IssueDist(issue,
                        haversineDistance(lat, lng, issue.getLatitude(), issue.getLongitude())))
                .filter(d -> d.distanceKm() <= radius)
                .toList();
    }

    /** Manual pagination using PageImpl after in-memory sort. */
    private Page<IssueResponseDto> toPage(List<IssueResponseDto> sorted, Pageable pageable) {
        int total = sorted.size();
        int start = (int) Math.min(pageable.getOffset(), total);
        int end   = Math.min(start + pageable.getPageSize(), total);
        return new PageImpl<>(sorted.subList(start, end), pageable, total);
    }

    /** Haversine formula — returns great-circle distance in kilometres. */
    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private IssueResponseDto mapToResponse(Issue issue, String requestingUserId) {
        return mapToResponse(issue, requestingUserId, null);
    }

    private IssueResponseDto mapToResponse(Issue issue, String requestingUserId, Double distanceKm) {
        boolean hasVoted = requestingUserId != null &&
                voteRepository.existsByIssueIdAndUserId(issue.getId(), requestingUserId);

        return IssueResponseDto.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .latitude(issue.getLatitude())
                .longitude(issue.getLongitude())
                .locationName(issue.getLocationName())
                .category(issue.getCategory())
                .status(issue.getStatus())
                .imageUrl(issue.getImageUrl())
                .createdBy(issue.getCreatedBy())
                .createdByUsername(issue.getCreatedByUsername())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .voteCount(voteRepository.countByIssueId(issue.getId()))
                .hasVoted(hasVoted)
                .distanceKm(distanceKm)
                .build();
    }

    /** Patches hasVoted onto cached DTOs without re-querying per issue — one bulk query. */
    private List<IssueResponseDto> hydrateHasVoted(List<IssueResponseDto> dtos, String userId) {
        if (userId == null || dtos.isEmpty()) return dtos;
        List<Long> ids = dtos.stream().map(IssueResponseDto::getId).toList();
        Set<Long> voted = voteRepository.findVotedIssueIds(userId, ids);
        return dtos.stream()
                .map(dto -> IssueResponseDto.builder()
                        .id(dto.getId())
                        .title(dto.getTitle())
                        .description(dto.getDescription())
                        .latitude(dto.getLatitude())
                        .longitude(dto.getLongitude())
                        .locationName(dto.getLocationName())
                        .category(dto.getCategory())
                        .status(dto.getStatus())
                        .imageUrl(dto.getImageUrl())
                        .createdBy(dto.getCreatedBy())
                        .createdByUsername(dto.getCreatedByUsername())
                        .createdAt(dto.getCreatedAt())
                        .updatedAt(dto.getUpdatedAt())
                        .voteCount(dto.getVoteCount())
                        .hasVoted(voted.contains(dto.getId()))
                        .distanceKm(dto.getDistanceKm())
                        .build())
                .toList();
    }

    /** Deletes all trending cache keys matching the prefix pattern. */
    public void evictTrendingCache() {
        try {
            Set<String> keys = redisTemplate.keys(TRENDING_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis eviction failed: {}", e.getMessage());
        }
    }
}
