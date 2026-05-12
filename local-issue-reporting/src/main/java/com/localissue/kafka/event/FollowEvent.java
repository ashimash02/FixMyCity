package com.localissue.kafka.event;

import com.localissue.entity.NotificationType;

import java.time.LocalDateTime;

public class FollowEvent extends NotificationEvent {

    public FollowEvent(String recipientUserId, String senderUserId, String senderUsername, String message) {
        super(recipientUserId, senderUserId, senderUsername, null, message, LocalDateTime.now(), NotificationType.FOLLOW);
    }

    // Required for Jackson deserialization
    public FollowEvent() {
        super();
    }
}
