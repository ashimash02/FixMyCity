package com.localissue.service;

import com.localissue.dto.VoteResponseDto;

public interface VoteService {

    VoteResponseDto addVote(Long issueId, String userId);

    long getVoteCount(Long issueId);
}
