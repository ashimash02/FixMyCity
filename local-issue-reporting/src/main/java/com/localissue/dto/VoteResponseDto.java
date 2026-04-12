package com.localissue.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoteResponseDto {

    private Long issueId;
    private long voteCount;
    private String message;
}
