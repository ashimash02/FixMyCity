package com.localissue.service.impl;

import com.localissue.dto.VoteResponseDto;
import com.localissue.entity.Issue;
import com.localissue.entity.Vote;
import com.localissue.exception.ResourceNotFoundException;
import com.localissue.repository.IssueRepository;
import com.localissue.repository.VoteRepository;
import com.localissue.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final IssueRepository issueRepository;

    @Override
    @Transactional
    public VoteResponseDto addVote(Long issueId, String userId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        if (voteRepository.existsByIssueIdAndUserId(issueId, userId)) {
            throw new IllegalStateException("User '" + userId + "' has already voted on this issue");
        }

        Vote vote = Vote.builder()
                .issue(issue)
                .userId(userId)
                .build();

        voteRepository.save(vote);

        long newCount = voteRepository.countByIssueId(issueId);

        return VoteResponseDto.builder()
                .issueId(issueId)
                .voteCount(newCount)
                .message("Vote recorded successfully")
                .build();
    }

    @Override
    public long getVoteCount(Long issueId) {
        return voteRepository.countByIssueId(issueId);
    }
}
