package com.kodbtw.service;

import com.kodbtw.dto.DashboardOverview;
import com.kodbtw.dto.DashboardStatsResponse;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.dto.analytics.DashboardAnalyticsResponse;
import com.kodbtw.dto.publicprofile.PublicAnalyticsDto;
import com.kodbtw.dto.publicprofile.PublicOverviewDto;
import com.kodbtw.dto.publicprofile.PublicPlatformStatDto;
import com.kodbtw.dto.publicprofile.PublicProfileResponse;
import com.kodbtw.dto.publicprofile.PublicSocialLinksDto;
import com.kodbtw.entity.Profile;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class PublicProfileService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9_-]{3,50}$");

    private final ProfileRepository profileRepository;
    private final PlatformStatsService platformStatsService;
    private final DashboardAnalyticsService dashboardAnalyticsService;

    public PublicProfileService(ProfileRepository profileRepository,
                                PlatformStatsService platformStatsService,
                                DashboardAnalyticsService dashboardAnalyticsService) {
        this.profileRepository = profileRepository;
        this.platformStatsService = platformStatsService;
        this.dashboardAnalyticsService = dashboardAnalyticsService;
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }

        String normalized = username.trim().toLowerCase(Locale.ROOT);
        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid username format. Must be 3-50 characters with lowercase letters, numbers, hyphens, and underscores only");
        }

        Profile profile = profileRepository.findByUsername(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("Developer profile '" + normalized + "' not found"));

        Long userId = profile.getUser().getId();

        DashboardStatsResponse dashboardStats = platformStatsService.getDashboardStats(userId);
        DashboardAnalyticsResponse analytics = dashboardAnalyticsService.getAnalytics(userId);

        PublicSocialLinksDto socialLinks = new PublicSocialLinksDto(
                profile.getGithubUrl(),
                profile.getLinkedinUrl(),
                profile.getPortfolioUrl()
        );

        PublicOverviewDto overview = mapOverview(dashboardStats.getOverview(), dashboardStats.getPlatforms());
        List<PublicPlatformStatDto> platforms = mapPlatforms(dashboardStats.getPlatforms());
        PublicAnalyticsDto publicAnalytics = new PublicAnalyticsDto(
                analytics.getDifficulty(),
                analytics.getPlatformComparison(),
                analytics.getContests()
        );

        return new PublicProfileResponse(
                profile.getUsername(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getCollege(),
                profile.getGraduationYear(),
                profile.getLocation(),
                socialLinks,
                overview,
                platforms,
                publicAnalytics
        );
    }

    private PublicOverviewDto mapOverview(DashboardOverview ov, List<PlatformStats> platforms) {
        if (ov == null) {
            return new PublicOverviewDto(0, 0, 0, 0, 0, null, null, null, null);
        }

        Integer bestRating = null;
        String bestRatingPlatform = null;
        if (platforms != null) {
            for (PlatformStats ps : platforms) {
                if (ps.getRating() != null) {
                    if (bestRating == null || ps.getRating() > bestRating) {
                        bestRating = ps.getRating();
                        bestRatingPlatform = ps.getPlatform() != null ? ps.getPlatform().name() : null;
                    }
                }
            }
        }

        return new PublicOverviewDto(
                ov.getTotalProblemsSolved() != null ? ov.getTotalProblemsSolved() : 0,
                ov.getEasySolved() != null ? ov.getEasySolved() : 0,
                ov.getMediumSolved() != null ? ov.getMediumSolved() : 0,
                ov.getHardSolved() != null ? ov.getHardSolved() : 0,
                ov.getContestsParticipated() != null ? ov.getContestsParticipated() : 0,
                bestRating,
                bestRatingPlatform,
                ov.getCurrentStreak(),
                ov.getLongestStreak()
        );
    }

    private List<PublicPlatformStatDto> mapPlatforms(List<PlatformStats> platformStatsList) {
        if (platformStatsList == null || platformStatsList.isEmpty()) {
            return new ArrayList<>();
        }

        List<PublicPlatformStatDto> result = new ArrayList<>(platformStatsList.size());
        for (PlatformStats ps : platformStatsList) {
            result.add(new PublicPlatformStatDto(
                    ps.getPlatform(),
                    ps.getUsername(),
                    ps.getProfileUrl(),
                    "REAL".equalsIgnoreCase(ps.getSource()),
                    ps.getTotalProblemsSolved(),
                    ps.getEasySolved(),
                    ps.getMediumSolved(),
                    ps.getHardSolved(),
                    ps.getRating(),
                    ps.getMaxRating(),
                    ps.getRank(),
                    ps.getContestsParticipated(),
                    ps.getCurrentStreak(),
                    ps.getLongestStreak(),
                    ps.getSource() != null ? ps.getSource() : "MOCK",
                    ps.getLastSyncedAt()
            ));
        }
        return result;
    }
}
