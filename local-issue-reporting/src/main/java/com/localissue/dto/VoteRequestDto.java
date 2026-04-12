package com.localissue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequestDto {

    @NotBlank(message = "userId is required")
    private String userId;
}
