package com.localissue.controller;

import com.localissue.dto.IssueRequestDto;
import com.localissue.dto.IssueResponseDto;
import com.localissue.dto.IssueStatusUpdateDto;
import com.localissue.dto.LocationFilter;
import com.localissue.dto.VoteResponseDto;
import com.localissue.service.IssueService;
import com.localissue.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;


@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;
    private final VoteService voteService;

    @PostMapping
    public ResponseEntity<IssueResponseDto> createIssue(
            @Valid @RequestBody IssueRequestDto requestDto,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(issueService.createIssue(requestDto, userId, username));
    }

    @GetMapping
    public ResponseEntity<Page<IssueResponseDto>> getAllIssues(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "25") double radius,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        LocationFilter location = (lat != null && lng != null) ? new LocationFilter(lat, lng, radius) : null;
        return ResponseEntity.ok(issueService.getAllIssues(pageable, userId, location));
    }

    @GetMapping("/trending")
    public ResponseEntity<Page<IssueResponseDto>> getTrendingIssues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "25") double radius,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        LocationFilter location = (lat != null && lng != null) ? new LocationFilter(lat, lng, radius) : null;
        return ResponseEntity.ok(issueService.getTrendingIssues(PageRequest.of(page, size), userId, location));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueResponseDto> getIssueById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        return ResponseEntity.ok(issueService.getIssueById(id, userId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<IssueResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody IssueStatusUpdateDto dto) {
        return ResponseEntity.ok(issueService.updateIssueStatus(id, dto));
    }

    @GetMapping("/my-posts")
    public ResponseEntity<Page<IssueResponseDto>> getMyIssues(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(issueService.getMyIssues(pageable, jwt.getSubject()));
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<VoteResponseDto> toggleVote(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(voteService.toggleVote(id, jwt.getSubject()));
    }
}
