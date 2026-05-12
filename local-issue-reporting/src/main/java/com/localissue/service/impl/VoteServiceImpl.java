package com.localissue.service.impl;

import com.localissue.dto.VoteResponseDto;
import com.localissue.entity.Issue;
import com.localissue.entity.Vote;
import com.localissue.exception.ResourceNotFoundException;
import com.localissue.kafka.producer.NotificationEventProducer;
import com.localissue.repository.IssueRepository;
import com.localissue.repository.UserProfileRepository;
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
    private final UserProfileRepository userProfileRepository;
    private final NotificationEventProducer notificationEventProducer;
    private final IssueServiceImpl issueServiceImpl;

    @Override
    @Transactional
    public VoteResponseDto toggleVote(Long issueId, String userId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        boolean alreadyVoted = voteRepository.existsByIssueIdAndUserId(issueId, userId);

        if (alreadyVoted) {
            voteRepository.deleteByIssueIdAndUserId(issueId, userId);
        } else {
            voteRepository.save(Vote.builder().issue(issue).userId(userId).build());

            if (issue.getCreatedBy() != null && !issue.getCreatedBy().equals(userId)) {
                userProfileRepository.findById(userId).ifPresent(sender ->
                    notificationEventProducer.publishVoteEvent(
                            issue.getCreatedBy(),
                            userId,
                            sender.getUsername(),
                            issue.getId(),
                            issue.getTitle())
                );
            }
        }

        issueServiceImpl.evictTrendingCache();

        return VoteResponseDto.builder()
                .issueId(issueId)
                .totalVotes(voteRepository.countByIssueId(issueId))
                .hasVoted(!alreadyVoted)
                .build();
    }
}