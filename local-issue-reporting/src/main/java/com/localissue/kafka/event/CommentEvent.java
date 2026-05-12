package com.localissue.kafka.event;

import com.localissue.entity.NotificationType;

import java.time.LocalDateTime;

public class CommentEvent extends NotificationEvent {

    public CommentEvent(String recipientUserId, String senderUserId, String senderUsername, Long issueId, String message) {
        super(recipientUserId, senderUserId, senderUsername, issueId, message, LocalDateTime.now(), NotificationType.COMMENT);
    }

    // Required for Jackson deserialization
    public CommentEvent() {
        super();
    }
}
