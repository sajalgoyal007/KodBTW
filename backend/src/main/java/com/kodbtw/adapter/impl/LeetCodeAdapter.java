package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.leetcode.LeetCodeClient;
import com.kodbtw.adapter.leetcode.dto.LeetCodeDto;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adapter for fetching real public statistics from LeetCode.
 *
 * <p>Uses a publicly reachable but undocumented GraphQL endpoint
 * ({@code https://leetcode.com/graphql}); LeetCode does not publish a public API
 * contract for this query. It is subject to change without notice and its use must
 * be reviewed against LeetCode's current terms.
 * No private user credentials, session cookies, or browser automation are used.
 */
@Component
public class LeetCodeAdapter implements PlatformAdapter {

    private final LeetCodeClient leetCodeClient;

    public LeetCodeAdapter(LeetCodeClient leetCodeClient) {
        this.leetCodeClient = leetCodeClient;
    }

    @Override
    public Platform getPlatform() {
        return Platform.LEETCODE;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        String username = account.getUsername();
        String profileUrl = (account.getProfileUrl() != null && !account.getProfileUrl().isBlank())
                ? account.getProfileUrl()
                : "https://leetcode.com/u/" + username + "/";

        LeetCodeDto.GraphQLData data = leetCodeClient.fetchUserProfile(username);
        LeetCodeDto.MatchedUser matchedUser = data.matchedUser();

        Integer totalProblemsSolved = null;
        Integer easySolved = null;
        Integer mediumSolved = null;
        Integer hardSolved = null;

        if (matchedUser.submitStatsGlobal() != null && matchedUser.submitStatsGlobal().acSubmissionNum() != null) {
            List<LeetCodeDto.SubmissionCount> counts = matchedUser.submitStatsGlobal().acSubmissionNum();
            for (LeetCodeDto.SubmissionCount sc : counts) {
                if ("All".equalsIgnoreCase(sc.difficulty())) {
                    totalProblemsSolved = sc.count();
                } else if ("Easy".equalsIgnoreCase(sc.difficulty())) {
                    easySolved = sc.count();
                } else if ("Medium".equalsIgnoreCase(sc.difficulty())) {
                    mediumSolved = sc.count();
                } else if ("Hard".equalsIgnoreCase(sc.difficulty())) {
                    hardSolved = sc.count();
                }
            }
        }

        Integer rank = null;
        if (matchedUser.profile() != null && matchedUser.profile().ranking() != null) {
            rank = matchedUser.profile().ranking();
        }

        Integer currentStreak = null;
        if (matchedUser.userCalendar() != null) {
            currentStreak = matchedUser.userCalendar().streak();
        }

        Integer rating = null;
        Integer contestsParticipated = null;

        if (data.userContestRanking() != null) {
            LeetCodeDto.UserContestRanking contest = data.userContestRanking();
            if (contest.rating() != null) {
                rating = (int) Math.round(contest.rating());
            }
            contestsParticipated = contest.attendedContestsCount();
            if (rank == null && contest.globalRanking() != null) {
                rank = contest.globalRanking();
            }
        }

        return PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username(username)
                .profileUrl(profileUrl)
                .totalProblemsSolved(totalProblemsSolved)
                .easySolved(easySolved)
                .mediumSolved(mediumSolved)
                .hardSolved(hardSolved)
                .rating(rating)
                .rank(rank)
                .contestsParticipated(contestsParticipated)
                .currentStreak(currentStreak)
                .longestStreak(null) // Not available in public user profile query
                .lastSyncedAt(LocalDateTime.now())
                .source("LEETCODE_REAL")
                .build();
    }
}
