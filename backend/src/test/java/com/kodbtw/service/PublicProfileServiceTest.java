package com.kodbtw.service;

import com.kodbtw.dto.DashboardOverview;
import com.kodbtw.dto.DashboardStatsResponse;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.dto.analytics.ContestAnalyticsDto;
import com.kodbtw.dto.analytics.DashboardAnalyticsResponse;
import com.kodbtw.dto.analytics.DifficultyAnalyticsDto;
import com.kodbtw.dto.analytics.DifficultyMetricDto;
import com.kodbtw.dto.publicprofile.PublicProfileResponse;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.Profile;
import com.kodbtw.entity.User;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PlatformStatsService platformStatsService;

    @Mock
    private DashboardAnalyticsService dashboardAnalyticsService;

    private PublicProfileService publicProfileService;

    @BeforeEach
    void setUp() {
        publicProfileService = new PublicProfileService(profileRepository, platformStatsService, dashboardAnalyticsService);
    }

    @Test
    void testGetPublicProfile_Success() {
        User user = new User("Alice", "alice@example.com", "password");
        // reflection or setter for ID
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", 101L);

        Profile profile = new Profile(user);
        profile.setUsername("alicedev");
        profile.setDisplayName("Alice Developer");
        profile.setBio("Code enthusiast");
        profile.setCollege("Stanford");
        profile.setGithubUrl("https://github.com/alice");

        when(profileRepository.findByUsername("alicedev")).thenReturn(Optional.of(profile));

        PlatformStats leetcodeStat = PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username("alice_lc")
                .profileUrl("https://leetcode.com/alice_lc")
                .totalProblemsSolved(150)
                .easySolved(50)
                .mediumSolved(80)
                .hardSolved(20)
                .rating(1600)
                .source("REAL")
                .build();

        DashboardOverview overview = new DashboardOverview(150, 50, 80, 20, 5, 7, 14, 1);
        DashboardStatsResponse dashboardStats = new DashboardStatsResponse(overview, List.of(leetcodeStat));
        when(platformStatsService.getDashboardStats(101L)).thenReturn(dashboardStats);

        DifficultyAnalyticsDto diff = new DifficultyAnalyticsDto(
                150,
                new DifficultyMetricDto(50, 33.3),
                new DifficultyMetricDto(80, 53.3),
                new DifficultyMetricDto(20, 13.3),
                Collections.emptyList()
        );
        DashboardAnalyticsResponse analytics = new DashboardAnalyticsResponse(diff, Collections.emptyList(), new ContestAnalyticsDto(5, Collections.emptyList()));
        when(dashboardAnalyticsService.getAnalytics(101L)).thenReturn(analytics);

        PublicProfileResponse response = publicProfileService.getPublicProfile("alicedev");

        assertNotNull(response);
        assertEquals("alicedev", response.getUsername());
        assertEquals("Alice Developer", response.getDisplayName());
        assertEquals("Stanford", response.getCollege());
        assertEquals("https://github.com/alice", response.getSocialLinks().getGithubUrl());
        assertEquals(150, response.getOverview().getTotalProblemsSolved());
        assertEquals(1, response.getPlatforms().size());
        assertEquals("REAL", response.getPlatforms().get(0).getSource());
        assertEquals("LEETCODE", response.getPlatforms().get(0).getPlatform().name());
    }

    @Test
    void testGetPublicProfile_NonExistent_ThrowsNotFound() {
        when(profileRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> publicProfileService.getPublicProfile("unknown"));
    }

    @Test
    void testGetPublicProfile_InvalidUsernameFormat_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> publicProfileService.getPublicProfile("a"));
        assertThrows(IllegalArgumentException.class, () -> publicProfileService.getPublicProfile("user@invalid!"));
        assertThrows(IllegalArgumentException.class, () -> publicProfileService.getPublicProfile(""));
    }
}
