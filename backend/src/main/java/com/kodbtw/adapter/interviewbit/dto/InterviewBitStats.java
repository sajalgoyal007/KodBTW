package com.kodbtw.adapter.interviewbit.dto;

/** Normalized InterviewBit statistics parsed from the public profile page. */
public record InterviewBitStats(
        Integer score,
        Integer rank,
        Integer totalSolved,
        Integer currentStreak
) {
}
