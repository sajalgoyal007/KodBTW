package com.kodbtw.service;

import com.kodbtw.dto.leaderboard.LeaderboardEntryDto;
import com.kodbtw.dto.leaderboard.LeaderboardPageResponse;
import com.kodbtw.dto.leaderboard.MyRankResponse;
import com.kodbtw.entity.LeaderboardUserCache;
import com.kodbtw.repository.LeaderboardUserCacheRepository;
import com.kodbtw.repository.ProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Reads from {@code leaderboard_user_cache} to serve paginated leaderboard responses.
 *
 * <p>Does NOT call any external platform APIs — all data comes from the snapshot/cache tables
 * populated by {@link LeaderboardSyncService}.
 *
 * <p>Sort options:
 * <ul>
 *   <li>{@code score} — weighted difficulty score DESC, then total_solved DESC (tie-break)</li>
 *   <li>{@code solved} — total_solved DESC, then weighted_score DESC (tie-break)</li>
 *   <li>{@code rating} — best_rating DESC, then weighted_score DESC (tie-break)</li>
 * </ul>
 *
 * <p>Data filter options:
 * <ul>
 *   <li>{@code real} — only entries with {@code real_data_only = true}</li>
 *   <li>{@code all} — all entries including mock-sourced data</li>
 * </ul>
 */
@Service
public class LeaderboardService {

    public static final String SORT_SCORE  = "score";
    public static final String SORT_SOLVED = "solved";
    public static final String SORT_RATING = "rating";

    public static final String FILTER_REAL = "real";
    public static final String FILTER_ALL  = "all";

    private static final int MAX_PAGE_SIZE = 100;

    private final LeaderboardUserCacheRepository cacheRepository;
    private final ProfileRepository profileRepository;

    public LeaderboardService(LeaderboardUserCacheRepository cacheRepository,
                              ProfileRepository profileRepository) {
        this.cacheRepository = cacheRepository;
        this.profileRepository = profileRepository;
    }

    // ── Global leaderboard ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LeaderboardPageResponse getGlobalLeaderboard(int page, int size, String sort, String dataFilter, Long currentUserId) {
        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));

        Page<LeaderboardUserCache> resultPage = FILTER_REAL.equalsIgnoreCase(dataFilter)
                ? cacheRepository.findAllRealOnly(pageable)
                : cacheRepository.findAllIncludingMock(pageable);

        return buildResponse(resultPage, page, size, sort, dataFilter, currentUserId);
    }

    // ── College leaderboard ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LeaderboardPageResponse getCollegeLeaderboard(String college, int page, int size, String sort, String dataFilter, Long currentUserId) {
        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));

        Page<LeaderboardUserCache> resultPage = FILTER_REAL.equalsIgnoreCase(dataFilter)
                ? cacheRepository.findByCollegeRealOnly(college, pageable)
                : cacheRepository.findByCollegeIncludingMock(college, pageable);

        return buildResponse(resultPage, page, size, sort, dataFilter, currentUserId);
    }

    // ── My rank ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public MyRankResponse getMyRank(Long userId) {
        Optional<LeaderboardUserCache> cacheOpt = cacheRepository.findByUserId(userId);
        if (cacheOpt.isEmpty()) {
            return MyRankResponse.notSynced(userId);
        }
        LeaderboardUserCache c = cacheOpt.get();

        // Rank = 1 + count of users with strictly higher score.
        Long globalRank = null;
        Long collegeRank = null;

        if (c.getRealDataOnly()) {
            globalRank = 1L + cacheRepository.countRealOnlyWithHigherScore(c.getWeightedScore());
            if (c.getCollege() != null && !c.getCollege().isBlank()) {
                collegeRank = 1L + cacheRepository.countCollegeRealOnlyWithHigherScore(c.getCollege(), c.getWeightedScore());
            }
        }

        return new MyRankResponse(
                userId,
                c.getDisplayName(),
                globalRank,
                collegeRank,
                c.getCollege(),
                c.getTotalSolved(),
                c.getWeightedScore(),
                c.getBestRating(),
                c.getBestRatingPlatform(),
                c.getHasMockData(),
                c.getRealDataOnly(),
                c.getLastSyncedAt()
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private LeaderboardPageResponse buildResponse(
            Page<LeaderboardUserCache> resultPage,
            int page, int size, String sort, String dataFilter,
            Long currentUserId) {

        List<LeaderboardUserCache> cachedUsers = resultPage.getContent();
        Map<Long, String> usernamesByUserId = profileRepository.findAllByUserIdIn(
                        cachedUsers.stream().map(LeaderboardUserCache::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), profile -> profile.getUsername()));

        List<LeaderboardEntryDto> entries = IntStream.range(0, cachedUsers.size())
                .mapToObj(i -> {
                    LeaderboardUserCache c = cachedUsers.get(i);
                    long rank = (long) (page * size) + i + 1;
                    return toEntry(c, rank, currentUserId, usernamesByUserId.get(c.getUserId()));
                })
                .toList();

        // My rank for the footer card
        Long myRank = null;
        if (currentUserId != null) {
            Optional<LeaderboardUserCache> myCache = cacheRepository.findByUserId(currentUserId);
            if (myCache.isPresent() && myCache.get().getRealDataOnly()) {
                myRank = 1L + cacheRepository.countRealOnlyWithHigherScore(myCache.get().getWeightedScore());
            }
        }

        LocalDateTime lastSynced = cacheRepository.findLatestSyncedAt();

        return new LeaderboardPageResponse(
                page,
                size,
                resultPage.getTotalElements(),
                lastSynced,
                entries,
                myRank
        );
    }

    private LeaderboardEntryDto toEntry(LeaderboardUserCache c, long rank, Long currentUserId, String username) {
        boolean isCurrentUser = currentUserId != null && currentUserId.equals(c.getUserId());
        return new LeaderboardEntryDto(
                rank,
                c.getUserId(),
                c.getDisplayName(),
                username,
                c.getCollege(),
                c.getTotalSolved(),
                c.getWeightedScore(),
                c.getBestRating(),
                c.getBestRatingPlatform(),
                c.getTotalContests(),
                c.getHasMockData(),
                c.getRealDataOnly(),
                isCurrentUser
        );
    }

    private Sort buildSort(String sort) {
        return switch (sort == null ? SORT_SCORE : sort.toLowerCase()) {
            case SORT_SOLVED -> Sort.by(Sort.Direction.DESC, "totalSolved")
                    .and(Sort.by(Sort.Direction.DESC, "weightedScore"));
            case SORT_RATING -> Sort.by(Sort.Direction.DESC, "bestRating")
                    .and(Sort.by(Sort.Direction.DESC, "weightedScore"));
            default -> Sort.by(Sort.Direction.DESC, "weightedScore")
                    .and(Sort.by(Sort.Direction.DESC, "totalSolved"));
        };
    }
}
