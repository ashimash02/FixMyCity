package com.localissue.controller;

import com.localissue.dto.BioUpdateDto;
import com.localissue.dto.IssueResponseDto;
import com.localissue.dto.UserProfileDto;
import com.localissue.entity.UserProfile;
import com.localissue.exception.ResourceNotFoundException;
import com.localissue.repository.IssueRepository;
import com.localissue.repository.UserProfileRepository;
import com.localissue.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileRepository userProfileRepository;
    private final IssueService issueService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMe(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");

        // Upsert: sync username/email from JWT on every login, preserve bio
        UserProfile profile = userProfileRepository.findById(userId)
                .map(existing -> {
                    existing.setUsername(username);
                    existing.setEmail(email);
                    return existing;
                })
                .orElse(UserProfile.builder()
                        .userId(userId)
                        .username(username)
                        .email(email)
                        .build());

        userProfileRepository.save(profile);

        return ResponseEntity.ok(toDto(profile));
    }

    @PatchMapping("/me/bio")
    public ResponseEntity<UserProfileDto> updateBio(
            @RequestBody BioUpdateDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setBio(dto.getBio());
        userProfileRepository.save(profile);

        return ResponseEntity.ok(toDto(profile));
    }

    // ── Public endpoints ─────────────────────────────────────────────────────

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getUser(@PathVariable String userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return ResponseEntity.ok(toDto(profile));
    }

    @GetMapping("/{userId}/issues")
    public ResponseEntity<Page<IssueResponseDto>> getUserIssues(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(issueService.getMyIssues(pageable, userId));
    }

    private UserProfileDto toDto(UserProfile p) {
        return UserProfileDto.builder()
                .userId(p.getUserId())
                .username(p.getUsername())
                .email(p.getEmail())
                .bio(p.getBio())
                .build();
    }
}
