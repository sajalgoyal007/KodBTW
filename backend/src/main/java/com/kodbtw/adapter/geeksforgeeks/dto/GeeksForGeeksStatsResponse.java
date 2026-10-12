package com.kodbtw.adapter.geeksforgeeks.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeeksForGeeksStatsResponse(
        String status,
        String message,
        String platform,
        String username,
        Boolean cached,
        Stats data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Stats(
            Integer totalSolved,
            Integer totalQuestions,
            Double acceptanceRate,
            DifficultyCounts byDifficulty
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DifficultyCounts(
            Integer school,
            Integer basic,
            Integer easy,
            Integer medium,
            Integer hard
    ) {
    }
}
