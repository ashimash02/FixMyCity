package com.localissue.client.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** Request body matching ai-service/app/main.py:SummariseRequest. */
@Data
@AllArgsConstructor
public class SummariseRequest {
    private String area;
    private List<String> issues;
}
