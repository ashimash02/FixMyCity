package com.localissue.rabbitmq.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IssueUpdatedEvent extends IssueEvent {

    /** Included so the AI consumer can use the latest title when building its summary prompt. */
    private String title;

    public IssueUpdatedEvent(Long issueId, String title, String locationName,
                             Double latitude, Double longitude,
                             String category, String status, String createdBy) {
        super("ISSUE_UPDATED", issueId, locationName, latitude, longitude, category, status, createdBy);
        this.title = title;
    }
}
