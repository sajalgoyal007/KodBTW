package com.kodbtw.adapter.codechef.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public record CodeChefApiResponse(
        Boolean success,
        Integer status,
        String handle,
        Profile profile,
        String message,
        String platform
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Profile(
            String profile,
            String name,
            Integer currentRating,
            Integer highestRating,
            String countryFlag,
            String countryName,
            Integer globalRank,
            Integer countryRank,
            String stars,
            Integer totalSolved
    ) {
    }
}
