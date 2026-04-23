package com.localissue.service.impl;

import com.localissue.dto.IssueRequestDto;
import com.localissue.dto.IssueResponseDto;
import com.localissue.dto.IssueStatusUpdateDto;
import com.localissue.entity.Issue;
import com.localissue.exception.ResourceNotFoundException;
import com.localissue.repository.IssueRepository;
import com.localissue.repository.VoteRepository;
import com.localissue.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private static final Set<String> VALID_STATUSES = Set.of(
            "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"
    );

    private final IssueRepository issueRepository;
    private final VoteRepository voteRepository;

    @Override
    public IssueResponseDto createIssue(IssueRequestDto requestDto, String userId, String username) {
        Issue issue = Issue.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .latitude(requestDto.getLatitude())
                .longitude(requestDto.getLongitude())
                .category(requestDto.getCategory())
                .imageUrl(requestDto.getImageUrl())
                .status("OPEN")
                .createdBy(userId)
                .createdByUsername(username)
                .build();

        return mapToResponse(issueRepository.save(issue));
    }

    @Override
    public Page<IssueResponseDto> getAllIssues(Pageable pageable) {
        return issueRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public IssueResponseDto getIssueById(Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));
        return mapToResponse(issue);
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
        return mapToResponse(issueRepository.save(issue));
    }

    @Override
    public List<IssueResponseDto> getNearbyIssues(double lat, double lng, double radiusKm) {
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (lng < -180 || lng > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        if (radiusKm <= 0) {
            throw new IllegalArgumentException("Radius must be greater than 0");
        }

        return issueRepository.findAll().stream()
                .filter(issue -> issue.getLatitude() != null && issue.getLongitude() != null)
                .filter(issue -> haversineDistance(lat, lng, issue.getLatitude(), issue.getLongitude()) <= radiusKm)
                .sorted((a, b) -> Double.compare(
                        haversineDistance(lat, lng, a.getLatitude(), a.getLongitude()),
                        haversineDistance(lat, lng, b.getLatitude(), b.getLongitude())
                ))
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Calculates the great-circle distance between two coordinates using the Haversine formula.
     * @return distance in kilometers
     */
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

    private IssueResponseDto mapToResponse(Issue issue) {
        return IssueResponseDto.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .latitude(issue.getLatitude())
                .longitude(issue.getLongitude())
                .category(issue.getCategory())
                .status(issue.getStatus())
                .imageUrl(issue.getImageUrl())
                .createdBy(issue.getCreatedBy())
                .createdByUsername(issue.getCreatedByUsername())
                .createdAt(issue.getCreatedAt())
                .voteCount(voteRepository.countByIssueId(issue.getId()))
                .build();
    }
}
