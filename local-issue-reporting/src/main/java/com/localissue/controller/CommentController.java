package com.localissue.controller;

import com.localissue.dto.CommentRequestDto;
import com.localissue.dto.CommentResponseDto;
import com.localissue.entity.Comment;
import com.localissue.entity.Issue;
import com.localissue.exception.ResourceNotFoundException;
import com.localissue.repository.CommentRepository;
import com.localissue.repository.IssueRepository;
import com.localissue.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/issues/{issueId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<Page<CommentResponseDto>> getComments(
            @PathVariable Long issueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponseDto> comments = commentRepository
                .findByIssueIdOrderByCreatedAtDesc(issueId, pageable)
                .map(this::toDto);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CommentResponseDto> addComment(
            @PathVariable Long issueId,
            @Valid @RequestBody CommentRequestDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        userProfileService.ensureExists(userId, username);

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .createdBy(userId)
                .createdByUsername(username)
                .issue(issue)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(commentRepository.save(comment)));
    }

    private CommentResponseDto toDto(Comment c) {
        return CommentResponseDto.builder()
                .id(c.getId())
                .content(c.getContent())
                .createdBy(c.getCreatedBy())
                .createdByUsername(c.getCreatedByUsername())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
