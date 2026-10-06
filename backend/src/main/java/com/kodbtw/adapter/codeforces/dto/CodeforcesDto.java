package com.kodbtw.adapter.codeforces.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Jackson-compatible DTOs for the Codeforces public API response.
 *
 * <p>API endpoint: {@code https://codeforces.com/api/user.info?handles=<handle>}
 * <p>Documentation: <a href="https://codeforces.com/apiHelp/methods#user.info">Codeforces API</a>
 */
public final class CodeforcesDto {

    private CodeforcesDto() {
    }

    /**
     * Top-level response wrapper from Codeforces API.
     * {@code status} is "OK" on success, "FAILED" on error.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiResponse(
            String status,
            String comment,
            List<UserInfo> result
    ) {}

    /**
     * Codeforces user information. Maps only the fields we actually use.
     *
     * <p>Available fields from the API that we map:
     * <ul>
     *   <li>{@code handle} - Codeforces user handle</li>
     *   <li>{@code rating} - Current rating (null if unrated)</li>
     *   <li>{@code maxRating} - Maximum achieved rating (null if unrated)</li>
     *   <li>{@code rank} - Current rank title, e.g. "expert", "grandmaster" (null if unrated)</li>
     *   <li>{@code maxRank} - Maximum achieved rank title (null if unrated)</li>
     *   <li>{@code contribution} - User's contribution score</li>
     *   <li>{@code friendOfCount} - Number of users who have this user as friend</li>
     *   <li>{@code registrationTimeSeconds} - Registration time in Unix seconds</li>
     * </ul>
     *
     * <p>Fields NOT available from user.info (must be null in PlatformStats):
     * totalProblemsSolved, easySolved, mediumSolved, hardSolved,
     * currentStreak, longestStreak, contestsParticipated.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserInfo(
            String handle,
            Integer rating,
            Integer maxRating,
            String rank,
            String maxRank,
            Integer contribution,
            Integer friendOfCount,
            Long registrationTimeSeconds
    ) {}
}
