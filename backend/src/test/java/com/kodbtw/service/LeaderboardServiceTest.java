package com.kodbtw.service;

import com.kodbtw.dto.leaderboard.LeaderboardEntryDto;
import com.kodbtw.dto.leaderboard.LeaderboardPageResponse;
import com.kodbtw.dto.leaderboard.MyRankResponse;
import com.kodbtw.entity.LeaderboardUserCache;
import com.kodbtw.entity.Profile;
import com.kodbtw.entity.User;
import com.kodbtw.repository.LeaderboardUserCacheRepository;
import com.kodbtw.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private LeaderboardUserCacheRepository cacheRepository;

    @Mock
    private ProfileRepository profileRepository;

    private LeaderboardService leaderboardService;

    @BeforeEach
    void setUp() {
        leaderboardService = new LeaderboardService(cacheRepository, profileRepository);
    }

    private LeaderboardUserCache buildCache(Long userId, String name, String college, int solved, double score, Integer rating, boolean realOnly, boolean hasMock) {
        LeaderboardUserCache c = new LeaderboardUserCache();
        c.setUserId(userId);
        User user = new User();
        user.setId(userId);
        c.setUser(user);
        c.setDisplayName(name);
        c.setCollege(college);
        c.setTotalSolved(solved);
        c.setWeightedScore(score);
        c.setBestRating(rating);
        c.setBestRatingPlatform("CODEFORCES");
        c.setTotalContests(5);
        c.setRealDataOnly(realOnly);
        c.setHasMockData(hasMock);
        c.setLastSyncedAt(LocalDateTime.now());
        return c;
    }

    @Test
    void testGetGlobalLeaderboard_RealOnlyFilter() {
        LeaderboardUserCache user1 = buildCache(1L, "Alice", "NSUT", 500, 1500.0, 1800, true, false);
        LeaderboardUserCache user2 = buildCache(2L, "Bob", "IITD", 300, 900.0, 1600, true, false);
        Page<LeaderboardUserCache> page = new PageImpl<>(List.of(user1, user2));

        when(cacheRepository.findAllRealOnly(any(Pageable.class))).thenReturn(page);
        when(cacheRepository.findLatestSyncedAt()).thenReturn(LocalDateTime.of(2026, 10, 6, 2, 0));

        LeaderboardPageResponse response = leaderboardService.getGlobalLeaderboard(0, 25, "score", "real", 1L);

        assertNotNull(response);
        assertEquals(2, response.getEntries().size());
        assertEquals(1, response.getEntries().get(0).getRank());
        assertTrue(response.getEntries().get(0).isCurrentUser());
        assertEquals("Alice", response.getEntries().get(0).getDisplayName());
        assertEquals(2, response.getEntries().get(1).getRank());
        assertFalse(response.getEntries().get(1).isCurrentUser());

        verify(cacheRepository).findAllRealOnly(any(Pageable.class));
        verify(cacheRepository, never()).findAllIncludingMock(any(Pageable.class));
    }

    @Test
    void testGetGlobalLeaderboardIncludesPublicProfileUsername() {
        LeaderboardUserCache user = buildCache(1L, "Alice", "NSUT", 500, 1500.0, 1800, true, false);
        Page<LeaderboardUserCache> page = new PageImpl<>(List.of(user));
        Profile profile = new Profile(user.getUser());
        profile.setUsername("alice-dev");

        when(cacheRepository.findAllRealOnly(any(Pageable.class))).thenReturn(page);
        when(profileRepository.findAllByUserIdIn(List.of(1L))).thenReturn(List.of(profile));

        LeaderboardPageResponse response = leaderboardService.getGlobalLeaderboard(0, 25, "score", "real", null);

        assertEquals("alice-dev", response.getEntries().get(0).getUsername());
    }

    @Test
    void testGetGlobalLeaderboard_AllFilter() {
        LeaderboardUserCache user1 = buildCache(1L, "Alice", "NSUT", 500, 1500.0, 1800, true, false);
        LeaderboardUserCache user2 = buildCache(2L, "Charlie", "DTU", 400, 1000.0, 1400, false, true);
        Page<LeaderboardUserCache> page = new PageImpl<>(List.of(user1, user2));

        when(cacheRepository.findAllIncludingMock(any(Pageable.class))).thenReturn(page);

        LeaderboardPageResponse response = leaderboardService.getGlobalLeaderboard(0, 25, "solved", "all", null);

        assertNotNull(response);
        assertEquals(2, response.getEntries().size());
        verify(cacheRepository).findAllIncludingMock(any(Pageable.class));
    }

    @Test
    void testGetCollegeLeaderboard() {
        LeaderboardUserCache user1 = buildCache(1L, "Alice", "NSUT", 500, 1500.0, 1800, true, false);
        Page<LeaderboardUserCache> page = new PageImpl<>(List.of(user1));

        when(cacheRepository.findByCollegeRealOnly(eq("NSUT"), any(Pageable.class))).thenReturn(page);

        LeaderboardPageResponse response = leaderboardService.getCollegeLeaderboard("NSUT", 0, 10, "score", "real", 1L);

        assertNotNull(response);
        assertEquals(1, response.getEntries().size());
        assertEquals("NSUT", response.getEntries().get(0).getCollege());
    }

    @Test
    void testGetMyRank_WhenSyncedAndRealOnly() {
        LeaderboardUserCache user1 = buildCache(1L, "Alice", "NSUT", 500, 1500.0, 1800, true, false);
        when(cacheRepository.findByUserId(1L)).thenReturn(Optional.of(user1));
        when(cacheRepository.countRealOnlyWithHigherScore(1500.0)).thenReturn(3L);
        when(cacheRepository.countCollegeRealOnlyWithHigherScore("NSUT", 1500.0)).thenReturn(1L);

        MyRankResponse myRank = leaderboardService.getMyRank(1L);

        assertNotNull(myRank);
        assertEquals(1L, myRank.getUserId());
        assertEquals(4L, myRank.getGlobalRank()); // 1 + 3
        assertEquals(2L, myRank.getCollegeRank()); // 1 + 1
        assertEquals("Alice", myRank.getDisplayName());
        assertEquals("NSUT", myRank.getCollege());
        assertTrue(myRank.getRealDataOnly());
    }

    @Test
    void testGetMyRank_WhenNotSynced() {
        when(cacheRepository.findByUserId(99L)).thenReturn(Optional.empty());

        MyRankResponse myRank = leaderboardService.getMyRank(99L);

        assertNotNull(myRank);
        assertEquals(99L, myRank.getUserId());
        assertNull(myRank.getGlobalRank());
        assertNull(myRank.getLastSyncedAt());
    }

    @Test
    void testGetMyRank_WhenHasMockDataOnly() {
        LeaderboardUserCache user2 = buildCache(2L, "Bob", "IITD", 200, 400.0, null, false, true);
        when(cacheRepository.findByUserId(2L)).thenReturn(Optional.of(user2));

        MyRankResponse myRank = leaderboardService.getMyRank(2L);

        assertNotNull(myRank);
        assertNull(myRank.getGlobalRank());
        assertFalse(myRank.getRealDataOnly());
        assertTrue(myRank.getHasMockData());
    }

    @Test
    void testGetGlobalLeaderboard_PaginationCalculatesRanksCorrectly() {
        LeaderboardUserCache user3 = buildCache(3L, "Charlie", "DTU", 450, 1200.0, 1700, true, false);
        LeaderboardUserCache user4 = buildCache(4L, "Dave", "NSUT", 400, 1100.0, 1650, true, false);
        Page<LeaderboardUserCache> page = new PageImpl<>(List.of(user3, user4));

        when(cacheRepository.findAllRealOnly(any(Pageable.class))).thenReturn(page);

        // Page 1 with size 2: entries should have ranks 3 and 4
        LeaderboardPageResponse response = leaderboardService.getGlobalLeaderboard(1, 2, "score", "real", null);

        assertNotNull(response);
        assertEquals(2, response.getEntries().size());
        assertEquals(3L, response.getEntries().get(0).getRank());
        assertEquals(4L, response.getEntries().get(1).getRank());
        assertEquals(1, response.getPage());
        assertEquals(2, response.getSize());
    }

    @Test
    void testGetGlobalLeaderboard_SortByRating() {
        LeaderboardUserCache user1 = buildCache(1L, "Alice", "NSUT", 500, 1500.0, 1900, true, false);
        Page<LeaderboardUserCache> page = new PageImpl<>(List.of(user1));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(cacheRepository.findAllRealOnly(pageableCaptor.capture())).thenReturn(page);

        leaderboardService.getGlobalLeaderboard(0, 10, "rating", "real", null);

        Pageable captured = pageableCaptor.getValue();
        Sort.Order ratingOrder = captured.getSort().getOrderFor("bestRating");
        assertNotNull(ratingOrder);
        assertTrue(ratingOrder.isDescending());
    }
}
