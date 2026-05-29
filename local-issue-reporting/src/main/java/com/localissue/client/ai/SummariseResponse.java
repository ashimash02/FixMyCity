package com.localissue.client.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response body matching ai-service/app/main.py:SummariseResponse. */
@Data
@NoArgsConstructor
public class SummariseResponse {
    private String area;
    private String summary;
    @JsonProperty("issue_count")
    private int issueCount;
}
