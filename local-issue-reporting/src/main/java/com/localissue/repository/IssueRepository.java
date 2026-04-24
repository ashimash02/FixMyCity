package com.localissue.repository;

import com.localissue.entity.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    // Cheap bounding-box pre-filter — reduces candidates before precise Haversine check
    @Query("SELECT i FROM Issue i WHERE " +
           "i.latitude IS NOT NULL AND i.longitude IS NOT NULL AND " +
           "i.latitude  BETWEEN :minLat AND :maxLat AND " +
           "i.longitude BETWEEN :minLng AND :maxLng")
    List<Issue> findWithinBoundingBox(
            @Param("minLat") double minLat, @Param("maxLat") double maxLat,
            @Param("minLng") double minLng, @Param("maxLng") double maxLng);

    // Trending — all issues, ordered by vote count (used when no location filter)
    @Query(
        value = "SELECT i.* FROM issues i LEFT JOIN votes v ON v.issue_id = i.id " +
                "GROUP BY i.id ORDER BY COUNT(v.id) DESC",
        countQuery = "SELECT COUNT(*) FROM issues",
        nativeQuery = true
    )
    Page<Issue> findAllOrderByVoteCountDesc(Pageable pageable);
}
