package com.localissue.kafka.event;

import com.localissue.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private String recipientUserId;
    private String senderUserId;
    private String senderUsername;
    private Long issueId;
    private String message;
    private LocalDateTime timestamp;
    private NotificationType type;
}
