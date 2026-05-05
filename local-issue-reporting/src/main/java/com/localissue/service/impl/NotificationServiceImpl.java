package com.localissue.service.impl;

import com.localissue.dto.NotificationDto;
import com.localissue.entity.Issue;
import com.localissue.entity.Notification;
import com.localissue.entity.NotificationType;
import com.localissue.entity.UserProfile;
import com.localissue.exception.ResourceNotFoundException;
import com.localissue.repository.NotificationRepository;
import com.localissue.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void notify(UserProfile recipient, UserProfile sender, NotificationType type, String message, Issue issue) {
        // Never notify yourself
        if (recipient.getUserId().equals(sender.getUserId())) return;

        notificationRepository.save(Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .type(type)
                .message(message)
                .issue(issue)
                .isRead(false)
                .build());
    }

    @Override
    public List<NotificationDto> getNotificationsForUser(UserProfile recipient) {
        return notificationRepository
                .findByRecipientOrderByCreatedAtDesc(recipient)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public long getUnreadCount(UserProfile recipient) {
        return notificationRepository.countByRecipientAndIsReadFalse(recipient);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, UserProfile recipient) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        // Guard: only the recipient can mark it read
        if (!n.getRecipient().getUserId().equals(recipient.getUserId())) {
            throw new SecurityException("Not your notification");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Override
    @Transactional
    public void markAllAsRead(UserProfile recipient) {
        notificationRepository.markAllReadForRecipient(recipient);
    }

    private NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .senderUsername(n.getSender().getUsername())
                .senderId(n.getSender().getUserId())
                .issueId(n.getIssue() != null ? n.getIssue().getId() : null)
                .build();
    }
}
