package com.kodbtw.adapter.atcoder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response from Kenkoooo's AtCoder accepted-count endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AtCoderAcRankResponse(
        Integer count,
        Integer rank
) {
}
