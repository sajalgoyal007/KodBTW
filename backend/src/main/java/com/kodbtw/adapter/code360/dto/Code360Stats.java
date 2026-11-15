package com.kodbtw.adapter.code360.dto;

/** Normalized Code360 statistics parsed from the public profile page. */
public record Code360Stats(
        Integer totalSolved,
        Integer rating,
        Integer contestsParticipated,
        Integer currentStreak,
        Integer longestStreak
) {
}
