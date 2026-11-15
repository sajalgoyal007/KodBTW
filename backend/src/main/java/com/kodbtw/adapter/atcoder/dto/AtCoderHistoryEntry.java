package com.kodbtw.adapter.atcoder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** One rated contest entry from AtCoder's public user history endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AtCoderHistoryEntry(
        @JsonProperty("IsRated") Boolean isRated,
        @JsonProperty("NewRating") Integer newRating
) {
}
