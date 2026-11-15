package com.kodbtw.adapter.hackerearth.dto;

/** Normalized HackerEarth statistics parsed from the public profile page. */
public record HackerEarthStats(
        Integer points,
        Integer totalSolved,
        Integer contestsParticipated,
        Integer badges
) {
}
