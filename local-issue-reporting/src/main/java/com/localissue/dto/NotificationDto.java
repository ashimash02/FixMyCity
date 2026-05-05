package com.localissue.dto;

import com.localissue.entity.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationDto {
    private Long id;
    private NotificationType type;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private String senderUsername;
    private String senderId;
    private Long issueId;
}
