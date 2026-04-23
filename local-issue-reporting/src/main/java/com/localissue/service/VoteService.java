package com.localissue.service;

import com.localissue.dto.VoteResponseDto;

public interface VoteService {

    VoteResponseDto toggleVote(Long issueId, String userId);
}
