package com.localissue.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class IssueResponseDto implements Serializable {

    private Long id;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String locationName;
    private String category;
    private String status;
    private String imageUrl;
    private String createdBy;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long voteCount;
    private boolean hasVoted;
    private Double distanceKm; // null when no location filter is active
}
