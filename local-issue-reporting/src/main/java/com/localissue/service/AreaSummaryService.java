package com.localissue.service;

import com.localissue.dto.AreaSummaryResponseDto;

public interface AreaSummaryService {

    /**
     * Returns a summary of issues within `radiusKm` of the given coordinates,
     * or the trending-issues summary if any of lat/lng is null.
     *
     * Result is read-through cached in Redis. Cache is invalidated by
     * SummaryCacheInvalidator whenever an issue event is consumed.
     */
    AreaSummaryResponseDto getSummary(Double lat, Double lng, Double radiusKm, String areaLabel);

    /** Cache key constants — shared with the invalidator so both agree on what to wipe. */
    String AREA_CACHE_PREFIX     = "summary:area:";
    String TRENDING_CACHE_KEY    = "summary:trending";
}
