package com.kodbtw.adapter.leetcode.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

public final class LeetCodeDto {

    private LeetCodeDto() {
    }

    public record GraphQLRequest(
            String query,
            Map<String, Object> variables
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GraphQLResponse(
            GraphQLData data,
            List<GraphQLError> errors
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GraphQLError(
            String message
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GraphQLData(
            MatchedUser matchedUser,
            UserContestRanking userContestRanking
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MatchedUser(
            String username,
            UserProfile profile,
            SubmitStatsGlobal submitStatsGlobal,
            UserCalendar userCalendar
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserProfile(
            Integer ranking
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubmitStatsGlobal(
            List<SubmissionCount> acSubmissionNum
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubmissionCount(
            String difficulty,
            Integer count
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserCalendar(
            Integer streak,
            Integer totalActiveDays
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserContestRanking(
            Double rating,
            Integer attendedContestsCount,
            Integer globalRanking
    ) {}
}
