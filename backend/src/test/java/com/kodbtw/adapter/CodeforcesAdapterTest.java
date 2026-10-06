package com.kodbtw.adapter;

import com.kodbtw.adapter.codeforces.CodeforcesClient;
import com.kodbtw.adapter.codeforces.dto.CodeforcesDto;
import com.kodbtw.adapter.impl.CodeforcesAdapter;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeforcesAdapterTest {

    @Mock
    private CodeforcesClient codeforcesClient;

    private CodeforcesAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CodeforcesAdapter(codeforcesClient);
    }

    @Test
    void shouldReturnPlatformCodeforces() {
        assertEquals(Platform.CODEFORCES, adapter.getPlatform());
    }

    @Test
    void shouldMapSuccessfulRealCodeforcesResponseCorrectly() {
        String handle = "tourist";
        PlatformAccount account = new PlatformAccount();
        account.setUsername(handle);
        account.setPlatform(Platform.CODEFORCES);

        CodeforcesDto.UserInfo mockInfo = new CodeforcesDto.UserInfo(
                "tourist", 3384, 4009, "legendary grandmaster", "tourist",
                112, 91104, 1265987288L
        );

        when(codeforcesClient.fetchUserInfo(handle)).thenReturn(mockInfo);

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.CODEFORCES, stats.getPlatform());
        assertEquals("tourist", stats.getUsername());
        assertEquals("https://codeforces.com/profile/tourist", stats.getProfileUrl());
        assertEquals(3384, stats.getRating());
        assertEquals(4009, stats.getRank()); // maxRating mapped to rank
        assertNull(stats.getTotalProblemsSolved(), "totalProblemsSolved not available from user.info");
        assertNull(stats.getEasySolved(), "easySolved not available from user.info");
        assertNull(stats.getMediumSolved(), "mediumSolved not available from user.info");
        assertNull(stats.getHardSolved(), "hardSolved not available from user.info");
        assertNull(stats.getContestsParticipated(), "contestsParticipated not available from user.info");
        assertNull(stats.getCurrentStreak(), "currentStreak not available from user.info");
        assertNull(stats.getLongestStreak(), "longestStreak not available from user.info");
        assertNotNull(stats.getLastSyncedAt());
        assertEquals("CODEFORCES_REAL", stats.getSource());
    }

    @Test
    void shouldMapUsernameCorrectly() {
        PlatformAccount account = new PlatformAccount();
        account.setUsername("jiangly");
        account.setPlatform(Platform.CODEFORCES);

        CodeforcesDto.UserInfo mockInfo = new CodeforcesDto.UserInfo(
                "jiangly", 3150, 3600, "legendary grandmaster", "legendary grandmaster",
                50, 20000, 1500000000L
        );

        when(codeforcesClient.fetchUserInfo("jiangly")).thenReturn(mockInfo);

        PlatformStats stats = adapter.fetchStats(account);
        assertEquals("jiangly", stats.getUsername());
    }

    @Test
    void shouldMapRatingCorrectly() {
        PlatformAccount account = new PlatformAccount();
        account.setUsername("rated_user");
        account.setPlatform(Platform.CODEFORCES);

        CodeforcesDto.UserInfo mockInfo = new CodeforcesDto.UserInfo(
                "rated_user", 1500, 1800, "specialist", "expert",
                5, 100, 1600000000L
        );

        when(codeforcesClient.fetchUserInfo("rated_user")).thenReturn(mockInfo);

        PlatformStats stats = adapter.fetchStats(account);
        assertEquals(1500, stats.getRating());
    }

    @Test
    void shouldMapRankAsMaxRating() {
        PlatformAccount account = new PlatformAccount();
        account.setUsername("ranked_user");
        account.setPlatform(Platform.CODEFORCES);

        CodeforcesDto.UserInfo mockInfo = new CodeforcesDto.UserInfo(
                "ranked_user", 1200, 1600, "pupil", "expert",
                0, 50, 1600000000L
        );

        when(codeforcesClient.fetchUserInfo("ranked_user")).thenReturn(mockInfo);

        PlatformStats stats = adapter.fetchStats(account);
        assertEquals(1600, stats.getRank()); // maxRating is mapped to rank
    }

    @Test
    void shouldReturnNullForUnsupportedStatisticsFromUnratedUser() {
        PlatformAccount account = new PlatformAccount();
        account.setUsername("unrated_user");
        account.setPlatform(Platform.CODEFORCES);

        // Unrated user: no rating, no maxRating, no rank
        CodeforcesDto.UserInfo mockInfo = new CodeforcesDto.UserInfo(
                "unrated_user", null, null, null, null,
                0, 1, 1700000000L
        );

        when(codeforcesClient.fetchUserInfo("unrated_user")).thenReturn(mockInfo);

        PlatformStats stats = adapter.fetchStats(account);

        assertNull(stats.getRating(), "Unrated user must have null rating");
        assertNull(stats.getRank(), "Unrated user must have null rank (maxRating)");
        assertNull(stats.getTotalProblemsSolved());
        assertNull(stats.getEasySolved());
        assertNull(stats.getMediumSolved());
        assertNull(stats.getHardSolved());
        assertNull(stats.getContestsParticipated());
        assertNull(stats.getCurrentStreak());
        assertNull(stats.getLongestStreak());
        assertEquals("CODEFORCES_REAL", stats.getSource());
    }

    @Test
    void shouldConstructDefaultProfileUrlWhenNotProvided() {
        PlatformAccount account = new PlatformAccount();
        account.setUsername("someuser");
        account.setPlatform(Platform.CODEFORCES);

        CodeforcesDto.UserInfo mockInfo = new CodeforcesDto.UserInfo(
                "someuser", 1400, 1500, "specialist", "specialist",
                0, 10, 1600000000L
        );

        when(codeforcesClient.fetchUserInfo("someuser")).thenReturn(mockInfo);

        PlatformStats stats = adapter.fetchStats(account);
        assertEquals("https://codeforces.com/profile/someuser", stats.getProfileUrl());
    }

    @Test
    void shouldPreserveCustomProfileUrl() {
        PlatformAccount account = new PlatformAccount();
        account.setUsername("customuser");
        account.setProfileUrl("https://codeforces.com/customuser-special");
        account.setPlatform(Platform.CODEFORCES);

        CodeforcesDto.UserInfo mockInfo = new CodeforcesDto.UserInfo(
                "customuser", 1800, 2000, "expert", "candidate master",
                10, 500, 1600000000L
        );

        when(codeforcesClient.fetchUserInfo("customuser")).thenReturn(mockInfo);

        PlatformStats stats = adapter.fetchStats(account);
        assertEquals("https://codeforces.com/customuser-special", stats.getProfileUrl());
        assertEquals("CODEFORCES_REAL", stats.getSource());
    }

    @Test
    void shouldPropagateResourceNotFoundExceptionWhenUserNotFound() {
        PlatformAccount account = new PlatformAccount();
        account.setUsername("missinguser");
        account.setPlatform(Platform.CODEFORCES);

        when(codeforcesClient.fetchUserInfo("missinguser"))
                .thenThrow(new ResourceNotFoundException("Codeforces user not found: missinguser"));

        assertThrows(ResourceNotFoundException.class, () -> adapter.fetchStats(account));
    }

    @Test
    void shouldPropagatePlatformApiExceptionOnApiFailureWithoutFallingBackToMock() {
        PlatformAccount account = new PlatformAccount();
        account.setUsername("timeout_user");
        account.setPlatform(Platform.CODEFORCES);

        when(codeforcesClient.fetchUserInfo("timeout_user"))
                .thenThrow(new PlatformApiException("Codeforces API timeout"));

        PlatformApiException ex = assertThrows(PlatformApiException.class,
                () -> adapter.fetchStats(account));
        assertEquals("Codeforces API timeout", ex.getMessage());
    }

    @Test
    void shouldNotFallBackToMockAfterRealApiFailure() {
        PlatformAccount account = new PlatformAccount();
        account.setUsername("failing_user");
        account.setPlatform(Platform.CODEFORCES);

        when(codeforcesClient.fetchUserInfo("failing_user"))
                .thenThrow(new PlatformApiException("Failed to communicate with Codeforces API"));

        // Must throw, NOT return mock stats
        PlatformApiException ex = assertThrows(PlatformApiException.class,
                () -> adapter.fetchStats(account));
        assertNotNull(ex);
    }

    @Test
    void shouldHaveCorrectSourceForRealData() {
        PlatformAccount account = new PlatformAccount();
        account.setUsername("sourcecheck");
        account.setPlatform(Platform.CODEFORCES);

        CodeforcesDto.UserInfo mockInfo = new CodeforcesDto.UserInfo(
                "sourcecheck", 1000, 1200, "newbie", "pupil",
                0, 5, 1700000000L
        );

        when(codeforcesClient.fetchUserInfo("sourcecheck")).thenReturn(mockInfo);

        PlatformStats stats = adapter.fetchStats(account);
        assertEquals("CODEFORCES_REAL", stats.getSource());
    }
}
