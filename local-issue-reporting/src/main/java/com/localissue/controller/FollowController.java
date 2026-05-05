package com.localissue.controller;

import com.localissue.dto.FollowResponseDto;
import com.localissue.entity.Follow;
import com.localissue.entity.UserProfile;
import com.localissue.exception.ResourceNotFoundException;
import com.localissue.repository.FollowRepository;
import com.localissue.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class FollowController {

    private final FollowRepository followRepository;
    private final UserProfileRepository userProfileRepository;

    @PostMapping("/{userId}/follow")
    @Transactional
    public ResponseEntity<Map<String, Object>> follow(
            @PathVariable String userId,
            @AuthenticationPrincipal Jwt jwt) {
        String followerId = jwt.getSubject();

        if (followerId.equals(userId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You cannot follow yourself"));
        }

        UserProfile follower = findUser(followerId);
        UserProfile following = findUser(userId);

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            return ResponseEntity.ok(buildStats(follower, following));
        }

        followRepository.save(Follow.builder()
                .follower(follower)
                .following(following)
                .build());

        return ResponseEntity.ok(buildStats(follower, following));
    }

    @DeleteMapping("/{userId}/follow")
    @Transactional
    public ResponseEntity<Map<String, Object>> unfollow(
            @PathVariable String userId,
            @AuthenticationPrincipal Jwt jwt) {
        String followerId = jwt.getSubject();

        UserProfile follower = findUser(followerId);
        UserProfile following = findUser(userId);

        followRepository.deleteByFollowerAndFollowing(follower, following);

        return ResponseEntity.ok(buildStats(follower, following));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<FollowResponseDto>> getFollowers(@PathVariable String userId) {
        UserProfile user = findUser(userId);
        List<FollowResponseDto> followers = followRepository.findByFollowing(user).stream()
                .map(f -> toDto(f.getFollower()))
                .toList();
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<FollowResponseDto>> getFollowing(@PathVariable String userId) {
        UserProfile user = findUser(userId);
        List<FollowResponseDto> following = followRepository.findByFollower(user).stream()
                .map(f -> toDto(f.getFollowing()))
                .toList();
        return ResponseEntity.ok(following);
    }

    @GetMapping("/{userId}/follow-status")
    public ResponseEntity<Map<String, Object>> getFollowStatus(
            @PathVariable String userId,
            @AuthenticationPrincipal Jwt jwt) {
        String currentUserId = jwt.getSubject();
        UserProfile current = findUser(currentUserId);
        UserProfile target = findUser(userId);

        boolean isFollowing = followRepository.existsByFollowerAndFollowing(current, target);
        long followerCount = followRepository.countByFollowing(target);
        long followingCount = followRepository.countByFollower(target);

        return ResponseEntity.ok(Map.of(
                "isFollowing", isFollowing,
                "followerCount", followerCount,
                "followingCount", followingCount
        ));
    }

    private Map<String, Object> buildStats(UserProfile follower, UserProfile following) {
        return Map.of(
                "isFollowing", followRepository.existsByFollowerAndFollowing(follower, following),
                "followerCount", followRepository.countByFollowing(following),
                "followingCount", followRepository.countByFollower(following)
        );
    }

    private UserProfile findUser(String userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private FollowResponseDto toDto(UserProfile p) {
        return FollowResponseDto.builder()
                .userId(p.getUserId())
                .username(p.getUsername())
                .build();
    }
}
