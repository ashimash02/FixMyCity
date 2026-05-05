package com.localissue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class IssueEditDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String locationName;
    private Double latitude;
    private Double longitude;
    private String category;
    private String imageUrl;
}
