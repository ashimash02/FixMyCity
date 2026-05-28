package com.localissue.rabbitmq.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IssueCreatedEvent extends IssueEvent {

    private String title;

    public IssueCreatedEvent(Long issueId, String title, String locationName,
                             Double latitude, Double longitude,
                             String category, String createdBy) {
        super("ISSUE_CREATED", issueId, locationName, latitude, longitude, category, "OPEN", createdBy);
        this.title = title;
    }
}
