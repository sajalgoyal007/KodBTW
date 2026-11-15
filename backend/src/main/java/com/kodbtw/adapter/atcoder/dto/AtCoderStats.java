package com.kodbtw.adapter.atcoder.dto;

/** Normalized AtCoder statistics assembled from the public AtCoder and Kenkoooo APIs. */
public record AtCoderStats(
        Integer rating,
        Integer maxRating,
        Integer rank,
        Integer totalSolved,
        Integer contestsParticipated,
        Integer currentStreak,
        Integer longestStreak
) {
}
