package com.localissue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueStatusUpdateDto {

    @NotBlank(message = "Status is required")
    private String status;
}
