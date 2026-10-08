package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.codeforces.CodeforcesClient;
import com.kodbtw.adapter.codeforces.dto.CodeforcesDto;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Adapter for fetching real public statistics from Codeforces.
 *
 * <p>Uses the official Codeforces public API endpoint:
 * {@code GET https://codeforces.com/api/user.info?handles=<handle>}
 *
 * <p>This is a fully public, unauthenticated API. No private credentials,
 * session cookies, or browser automation are used.
 *
 * <p>Fields available from user.info and mapped:
 * <ul>
 *   <li>{@code rating} - current Codeforces rating</li>
 *   <li>{@code maxRating} - mapped to {@code rank} (highest rating achieved)</li>
 *   <li>{@code rank} - current rank title (e.g. "expert"), stored in source metadata</li>
 * </ul>
 *
 * <p>Fields NOT available from user.info (returned as null):
 * totalProblemsSolved, easySolved, mediumSolved, hardSolved,
 * currentStreak, longestStreak, contestsParticipated.
 */
@Component
public class CodeforcesAdapter implements PlatformAdapter {

    private final CodeforcesClient codeforcesClient;

    public CodeforcesAdapter(CodeforcesClient codeforcesClient) {
        this.codeforcesClient = codeforcesClient;
    }

    @Override
    public Platform getPlatform() {
        return Platform.CODEFORCES;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        String handle = account.getUsername();
        String profileUrl = (account.getProfileUrl() != null && !account.getProfileUrl().isBlank())
                ? account.getProfileUrl()
                : "https://codeforces.com/profile/" + handle;

        CodeforcesDto.UserInfo userInfo = codeforcesClient.fetchUserInfo(handle);

        // Map available fields from user.info
        Integer rating = userInfo.rating();
        Integer maxRating = userInfo.maxRating();

        return PlatformStats.builder()
                .platform(Platform.CODEFORCES)
                .username(handle)
                .profileUrl(profileUrl)
                .totalProblemsSolved(null)   // Not available from user.info
                .easySolved(null)            // Not available from user.info
                .mediumSolved(null)          // Not available from user.info
                .hardSolved(null)            // Not available from user.info
                .rating(rating)
                .maxRating(maxRating)
                .rank(null)                  // The API's rank is a title, not a numeric global rank
                .contestsParticipated(null)  // Not available from user.info
                .currentStreak(null)         // Not available from user.info
                .longestStreak(null)         // Not available from user.info
                .lastSyncedAt(LocalDateTime.now())
                .source("CODEFORCES_REAL")
                .build();
    }
}
