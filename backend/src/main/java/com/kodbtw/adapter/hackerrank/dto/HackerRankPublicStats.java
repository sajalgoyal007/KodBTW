package com.kodbtw.adapter.hackerrank.dto;

/**
 * Normalized HackerRank public REST data.
 *
 * <p>HackerRank exposes per-domain badge progress through
 * {@code /rest/hackers/{handle}/badges} and per-track scores through
 * {@code /rest/hackers/{handle}/scores_elo}. Solved counts are summed across the badge
 * domains that report a solved value. The ELO-derived score is exposed as a rating; HackerRank
 * does not publish a public rating history for practice tracks, so the same value is used as the
 * maximum rating. Badge and star counts are kept for diagnostics and are not added to totals.
 */
public record HackerRankPublicStats(
        Integer totalSolved,
        Integer badgeCount,
        Integer totalStars,
        Integer rating,
        Integer maxRating
) {
}
