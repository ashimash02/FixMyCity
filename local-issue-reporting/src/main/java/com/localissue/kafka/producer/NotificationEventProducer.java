package com.localissue.kafka.producer;

import com.localissue.config.KafkaTopicConfig;
import com.localissue.kafka.event.CommentEvent;
import com.localissue.kafka.event.FollowEvent;
import com.localissue.kafka.event.NotificationEvent;
import com.localissue.kafka.event.VoteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public void publishFollowEvent(String recipientUserId, String senderUserId, String senderUsername) {
        FollowEvent event = new FollowEvent(
                recipientUserId,
                senderUserId,
                senderUsername,
                senderUsername + " started following you"
        );
        publish(event);
    }

    public void publishCommentEvent(String recipientUserId, String senderUserId, String senderUsername,
                                    Long issueId, String issueTitle) {
        CommentEvent event = new CommentEvent(
                recipientUserId,
                senderUserId,
                senderUsername,
                issueId,
                senderUsername + " commented on your issue: \"" + issueTitle + "\""
        );
        publish(event);
    }

    public void publishVoteEvent(String recipientUserId, String senderUserId, String senderUsername,
                                 Long issueId, String issueTitle) {
        VoteEvent event = new VoteEvent(
                recipientUserId,
                senderUserId,
                senderUsername,
                issueId,
                senderUsername + " voted on your issue: \"" + issueTitle + "\""
        );
        publish(event);
    }

    private void publish(NotificationEvent event) {
        kafkaTemplate.send(KafkaTopicConfig.NOTIFICATIONS_TOPIC, event.getRecipientUserId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} event for recipient {}: {}",
                                event.getType(), event.getRecipientUserId(), ex.getMessage());
                    } else {
                        log.debug("Published {} event to partition {} offset {}",
                                event.getType(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
