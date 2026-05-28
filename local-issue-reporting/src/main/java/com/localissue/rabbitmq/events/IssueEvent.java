package com.localissue.rabbitmq.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base for all issue domain events published to RabbitMQ.
 *
 * @JsonTypeInfo uses the existing eventType field as the Jackson type discriminator,
 * so the JSON payload stays flat — no extra "_type" wrapper field.
 */
@Data
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = IssueCreatedEvent.class,  name = "ISSUE_CREATED"),
        @JsonSubTypes.Type(value = IssueUpdatedEvent.class,  name = "ISSUE_UPDATED"),
        @JsonSubTypes.Type(value = IssueResolvedEvent.class, name = "ISSUE_RESOLVED")
})
public abstract class IssueEvent {

    /** Unique ID per event — consumers can use this for idempotency checks. */
    private String eventId;

    private String eventType;
    private Long issueId;
    private String locationName;
    /** Coordinates of the issue — used by the consumer to geo-query nearby issues for summary generation. */
    private Double latitude;
    private Double longitude;
    private String category;
    private String status;
    private String createdBy;
    private LocalDateTime occurredAt;

    protected IssueEvent(String eventType, Long issueId, String locationName,
                         Double latitude, Double longitude,
                         String category, String status, String createdBy) {
        this.eventId      = UUID.randomUUID().toString();
        this.eventType    = eventType;
        this.issueId      = issueId;
        this.locationName = locationName;
        this.latitude     = latitude;
        this.longitude    = longitude;
        this.category     = category;
        this.status       = status;
        this.createdBy    = createdBy;
        this.occurredAt   = LocalDateTime.now();
    }
}
