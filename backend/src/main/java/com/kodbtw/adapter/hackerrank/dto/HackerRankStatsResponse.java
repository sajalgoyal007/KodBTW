package com.kodbtw.adapter.hackerrank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HackerRankStatsResponse(
        String status,
        String message,
        String platform,
        String username,
        Boolean cached,
        Stats data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Stats(
            Integer totalSolved
    ) {
    }
}
