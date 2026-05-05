package com.localissue.repository;

import com.localissue.entity.Follow;
import com.localissue.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerAndFollowing(UserProfile follower, UserProfile following);

    void deleteByFollowerAndFollowing(UserProfile follower, UserProfile following);

    List<Follow> findByFollower(UserProfile follower);

    List<Follow> findByFollowing(UserProfile following);

    long countByFollower(UserProfile follower);

    long countByFollowing(UserProfile following);

    // All userIds that the given user follows — used to build the following feed
    @Query("SELECT f.following.userId FROM Follow f WHERE f.follower.userId = :followerId")
    List<String> findFollowingIdsByFollowerId(@Param("followerId") String followerId);
}
