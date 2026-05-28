package com.localissue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AreaSummaryResponseDto {

    public enum Mode { AREA, TRENDING }

    private String area;
    private String summary;
    private int issueCount;
    private boolean cached;
    private Mode mode;
}
