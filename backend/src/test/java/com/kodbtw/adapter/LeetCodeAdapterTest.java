package com.kodbtw.adapter;

import com.kodbtw.adapter.impl.LeetCodeAdapter;
import com.kodbtw.adapter.leetcode.LeetCodeClient;
import com.kodbtw.adapter.leetcode.dto.LeetCodeDto;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeetCodeAdapterTest {

    @Mock
    private LeetCodeClient leetCodeClient;

    private LeetCodeAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LeetCodeAdapter(leetCodeClient);
    }

    @Test
    void shouldReturnPlatformLeetCode() {
        assertEquals(Platform.LEETCODE, adapter.getPlatform());
    }

    @Test
    void shouldMapSuccessfulRealLeetCodeResponseCorrectly() {
        String username = "lee215";
        PlatformAccount account = new PlatformAccount();
        account.setUsername(username);
        account.setPlatform(Platform.LEETCODE);

        LeetCodeDto.GraphQLData mockData = new LeetCodeDto.GraphQLData(
                new LeetCodeDto.MatchedUser(
                        username,
                        new LeetCodeDto.UserProfile(103463),
                        new LeetCodeDto.SubmitStatsGlobal(List.of(
                                new LeetCodeDto.SubmissionCount("All", 686),
                                new LeetCodeDto.SubmissionCount("Easy", 126),
                                new LeetCodeDto.SubmissionCount("Medium", 411),
                                new LeetCodeDto.SubmissionCount("Hard", 149)
                        )),
                        new LeetCodeDto.UserCalendar(3, 49)
                ),
                new LeetCodeDto.UserContestRanking(2204.316, 27, 7550)
        );

        when(leetCodeClient.fetchUserProfile(username)).thenReturn(mockData);

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.LEETCODE, stats.getPlatform());
        assertEquals(username, stats.getUsername());
        assertEquals("https://leetcode.com/u/lee215/", stats.getProfileUrl());
        assertEquals(686, stats.getTotalProblemsSolved());
        assertEquals(126, stats.getEasySolved());
        assertEquals(411, stats.getMediumSolved());
        assertEquals(149, stats.getHardSolved());
        assertEquals(2204, stats.getRating());
        assertEquals(103463, stats.getRank());
        assertEquals(27, stats.getContestsParticipated());
        assertEquals(3, stats.getCurrentStreak());
        assertNull(stats.getLongestStreak(), "Longest streak should be null when not available");
        assertNotNull(stats.getLastSyncedAt());
        assertEquals("LEETCODE_REAL", stats.getSource());
    }

    @Test
    void shouldPreserveCustomProfileUrl() {
        String username = "customuser";
        PlatformAccount account = new PlatformAccount();
        account.setUsername(username);
        account.setProfileUrl("https://leetcode.com/customuser-special");
        account.setPlatform(Platform.LEETCODE);

        LeetCodeDto.GraphQLData mockData = new LeetCodeDto.GraphQLData(
                new LeetCodeDto.MatchedUser(
                        username,
                        new LeetCodeDto.UserProfile(500),
                        new LeetCodeDto.SubmitStatsGlobal(List.of(
                                new LeetCodeDto.SubmissionCount("All", 50)
                        )),
                        null
                ),
                null
        );

        when(leetCodeClient.fetchUserProfile(username)).thenReturn(mockData);

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals("https://leetcode.com/customuser-special", stats.getProfileUrl());
        assertEquals("LEETCODE_REAL", stats.getSource());
    }

    @Test
    void shouldHandleMissingMetricsAsNullWithoutFakeValues() {
        String username = "unrateduser";
        PlatformAccount account = new PlatformAccount();
        account.setUsername(username);
        account.setPlatform(Platform.LEETCODE);

        // User with minimal data: no contest rankings, no calendar, no profile rank
        LeetCodeDto.GraphQLData mockData = new LeetCodeDto.GraphQLData(
                new LeetCodeDto.MatchedUser(
                        username,
                        null,
                        new LeetCodeDto.SubmitStatsGlobal(List.of(
                                new LeetCodeDto.SubmissionCount("All", 10),
                                new LeetCodeDto.SubmissionCount("Easy", 10),
                                new LeetCodeDto.SubmissionCount("Medium", 0),
                                new LeetCodeDto.SubmissionCount("Hard", 0)
                        )),
                        null
                ),
                null
        );

        when(leetCodeClient.fetchUserProfile(username)).thenReturn(mockData);

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(10, stats.getTotalProblemsSolved());
        assertEquals(10, stats.getEasySolved());
        assertEquals(0, stats.getMediumSolved());
        assertEquals(0, stats.getHardSolved());
        assertNull(stats.getRating(), "Unrated user must have null rating, not fake default");
        assertNull(stats.getRank(), "Missing rank must be null");
        assertNull(stats.getContestsParticipated(), "Missing contests count must be null");
        assertNull(stats.getCurrentStreak(), "Missing streak must be null");
        assertNull(stats.getLongestStreak(), "Longest streak must be null");
        assertEquals("LEETCODE_REAL", stats.getSource());
    }

    @Test
    void shouldPropagateResourceNotFoundExceptionWhenUserNotFound() {
        String username = "missinguser";
        PlatformAccount account = new PlatformAccount();
        account.setUsername(username);
        account.setPlatform(Platform.LEETCODE);

        when(leetCodeClient.fetchUserProfile(username))
                .thenThrow(new ResourceNotFoundException("LeetCode user not found: " + username));

        assertThrows(ResourceNotFoundException.class, () -> adapter.fetchStats(account));
    }

    @Test
    void shouldPropagatePlatformApiExceptionOnTimeoutOrExternalFailureWithoutFallingBackToMock() {
        String username = "timeoutuser";
        PlatformAccount account = new PlatformAccount();
        account.setUsername(username);
        account.setPlatform(Platform.LEETCODE);

        when(leetCodeClient.fetchUserProfile(username))
                .thenThrow(new PlatformApiException("LeetCode API timeout"));

        PlatformApiException ex = assertThrows(PlatformApiException.class, () -> adapter.fetchStats(account));
        assertEquals("LeetCode API timeout", ex.getMessage());
    }
}
