package com.localissue.kafka.consumer;

import com.localissue.config.KafkaTopicConfig;
import com.localissue.entity.Issue;
import com.localissue.entity.UserProfile;
import com.localissue.kafka.event.NotificationEvent;
import com.localissue.repository.IssueRepository;
import com.localissue.repository.UserProfileRepository;
import com.localissue.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final UserProfileRepository userProfileRepository;
    private final IssueRepository issueRepository;

    @KafkaListener(topics = KafkaTopicConfig.NOTIFICATIONS_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consume(NotificationEvent event) {
        try {
            UserProfile recipient = userProfileRepository.findById(event.getRecipientUserId())
                    .orElse(null);
            UserProfile sender = userProfileRepository.findById(event.getSenderUserId())
                    .orElse(null);

            if (recipient == null || sender == null) {
                log.warn("Skipping {} event — recipient or sender not found (recipient={}, sender={})",
                        event.getType(), event.getRecipientUserId(), event.getSenderUserId());
                return;
            }

            Issue issue = null;
            if (event.getIssueId() != null) {
                issue = issueRepository.findById(event.getIssueId()).orElse(null);
                if (issue == null) {
                    log.warn("Skipping {} event — issue {} not found", event.getType(), event.getIssueId());
                    return;
                }
            }

            notificationService.notify(recipient, sender, event.getType(), event.getMessage(), issue);

            log.debug("Processed {} notification for recipient {}", event.getType(), event.getRecipientUserId());

        } catch (Exception e) {
            log.error("Failed to process {} event for recipient {}: {}",
                    event.getType(), event.getRecipientUserId(), e.getMessage(), e);
        }
    }
}
