package com.localissue.rabbitmq.producer;

import com.localissue.config.RabbitMQConfig;
import com.localissue.entity.Issue;
import com.localissue.rabbitmq.events.IssueCreatedEvent;
import com.localissue.rabbitmq.events.IssueEvent;
import com.localissue.rabbitmq.events.IssueResolvedEvent;
import com.localissue.rabbitmq.events.IssueUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishCreated(Issue issue) {
        publish(
            RabbitMQConfig.ROUTING_KEY_CREATED,
            new IssueCreatedEvent(
                issue.getId(),
                issue.getTitle(),
                issue.getLocationName(),
                issue.getLatitude(),
                issue.getLongitude(),
                issue.getCategory(),
                issue.getCreatedBy()
            )
        );
    }

    public void publishUpdated(Issue issue) {
        publish(
            RabbitMQConfig.ROUTING_KEY_UPDATED,
            new IssueUpdatedEvent(
                issue.getId(),
                issue.getTitle(),
                issue.getLocationName(),
                issue.getLatitude(),
                issue.getLongitude(),
                issue.getCategory(),
                issue.getStatus(),
                issue.getCreatedBy()
            )
        );
    }

    public void publishResolved(Issue issue) {
        publish(
            RabbitMQConfig.ROUTING_KEY_RESOLVED,
            new IssueResolvedEvent(
                issue.getId(),
                issue.getLocationName(),
                issue.getLatitude(),
                issue.getLongitude(),
                issue.getCategory(),
                issue.getStatus(),
                issue.getCreatedBy()
            )
        );
    }

    /**
     * Fire-and-forget publish. Exceptions are caught and logged rather than rethrown
     * so that a RabbitMQ outage never rolls back an otherwise successful HTTP request.
     */
    private void publish(String routingKey, IssueEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.ISSUES_EXCHANGE, routingKey, event);
            log.debug("Published {} for issue {} (area='{}', lat={}, lng={})",
                    event.getEventType(), event.getIssueId(), event.getLocationName(),
                    event.getLatitude(), event.getLongitude());
        } catch (Exception e) {
            log.error("Failed to publish {} for issue {}: {}",
                    event.getEventType(), event.getIssueId(), e.getMessage());
        }
    }
}
