package com.localissue.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowResponseDto {
    private String userId;
    private String username;
}
