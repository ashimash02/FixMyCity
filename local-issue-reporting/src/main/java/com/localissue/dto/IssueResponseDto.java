package com.localissue.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class IssueResponseDto {

    private Long id;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String category;
    private String status;
    private String imageUrl;
    private LocalDateTime createdAt;
    private long voteCount;
}
