package com.localissue.service;

import com.localissue.dto.VoteRequestDto;
import com.localissue.dto.VoteResponseDto;

public interface VoteService {

    VoteResponseDto addVote(Long issueId, VoteRequestDto requestDto);

    long getVoteCount(Long issueId);
}
