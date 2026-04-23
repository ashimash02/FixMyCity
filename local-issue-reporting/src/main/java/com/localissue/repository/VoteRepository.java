package com.localissue.repository;

import com.localissue.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    long countByIssueId(Long issueId);

    boolean existsByIssueIdAndUserId(Long issueId, String userId);

    void deleteByIssueIdAndUserId(Long issueId, String userId);
}
