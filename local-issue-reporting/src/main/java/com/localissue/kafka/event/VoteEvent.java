package com.localissue.kafka.event;

import com.localissue.entity.NotificationType;

import java.time.LocalDateTime;

public class VoteEvent extends NotificationEvent {

    public VoteEvent(String recipientUserId, String senderUserId, String senderUsername, Long issueId, String message) {
        super(recipientUserId, senderUserId, senderUsername, issueId, message, LocalDateTime.now(), NotificationType.VOTE);
    }

    // Required for Jackson deserialization
    public VoteEvent() {
        super();
    }
}
