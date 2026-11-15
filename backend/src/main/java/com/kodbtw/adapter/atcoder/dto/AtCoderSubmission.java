package com.kodbtw.adapter.atcoder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** One submission row from Kenkoooo's AtCoder submissions endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AtCoderSubmission(
        @JsonProperty("epoch_second") Long epochSecond,
        @JsonProperty("problem_id") String problemId,
        String result
) {
}
