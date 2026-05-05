package com.localissue.controller;

import com.localissue.dto.NotificationDto;
import com.localissue.entity.UserProfile;
import com.localissue.exception.ResourceNotFoundException;
import com.localissue.repository.UserProfileRepository;
import com.localissue.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserProfileRepository userProfileRepository;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(@AuthenticationPrincipal Jwt jwt) {
        UserProfile user = resolveUser(jwt);
        return ResponseEntity.ok(notificationService.getNotificationsForUser(user));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        UserProfile user = resolveUser(jwt);
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(user)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        notificationService.markAsRead(id, resolveUser(jwt));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        notificationService.markAllAsRead(resolveUser(jwt));
        return ResponseEntity.noContent().build();
    }

    private UserProfile resolveUser(Jwt jwt) {
        return userProfileRepository.findById(jwt.getSubject())
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
    }
}
