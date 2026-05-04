package com.localissue.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileDto {
    private String userId;
    private String username;
    private String email;
    private String bio;
}
