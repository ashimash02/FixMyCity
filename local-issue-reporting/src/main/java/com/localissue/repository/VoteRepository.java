package com.localissue.repository;

import com.localissue.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    long countByIssueId(Long issueId);

    boolean existsByIssueIdAndUserId(Long issueId, String userId);

    void deleteByIssueIdAndUserId(Long issueId, String userId);

    @Query("SELECT v.issue.id FROM Vote v WHERE v.userId = :userId AND v.issue.id IN :issueIds")
    Set<Long> findVotedIssueIds(@Param("userId") String userId, @Param("issueIds") List<Long> issueIds);
}
