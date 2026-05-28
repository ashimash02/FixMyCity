package com.localissue.rabbitmq.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IssueResolvedEvent extends IssueEvent {

    /**
     * The terminal status that triggered this event.
     * Carried explicitly so the AI consumer can filter on RESOLVED vs CLOSED
     * if it wants to treat them differently in future.
     */
    private String resolvedStatus;

    public IssueResolvedEvent(Long issueId, String locationName,
                              Double latitude, Double longitude,
                              String category, String status, String createdBy) {
        super("ISSUE_RESOLVED", issueId, locationName, latitude, longitude, category, status, createdBy);
        this.resolvedStatus = status;
    }
}
