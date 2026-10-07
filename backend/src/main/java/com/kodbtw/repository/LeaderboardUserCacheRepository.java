package com.kodbtw.repository;

import com.kodbtw.entity.LeaderboardUserCache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaderboardUserCacheRepository extends JpaRepository<LeaderboardUserCache, Long> {

    // ── Global leaderboard queries ─────────────────────────────────────────────

    /**
     * Real-only: excludes entries where hasMockData=true AND realDataOnly=false.
     * An entry qualifies for the real leaderboard if real_data_only=true, OR
     * if it has at least some real data (has_mock_data may be true but real_data_only need not be).
     * We use the simpler rule: exclude entries where ALL platforms are mock (realDataOnly=false AND totalSolved from real = 0).
     * Concretely: include only rows where real_data_only = true.
     */
    @Query("SELECT c FROM LeaderboardUserCache c WHERE c.realDataOnly = true")
    Page<LeaderboardUserCache> findAllRealOnly(Pageable pageable);

    @Query("SELECT c FROM LeaderboardUserCache c")
    Page<LeaderboardUserCache> findAllIncludingMock(Pageable pageable);

    // ── College leaderboard queries ────────────────────────────────────────────

    @Query("SELECT c FROM LeaderboardUserCache c WHERE c.realDataOnly = true AND LOWER(c.college) LIKE LOWER(CONCAT('%', :college, '%'))")
    Page<LeaderboardUserCache> findByCollegeRealOnly(@Param("college") String college, Pageable pageable);

    @Query("SELECT c FROM LeaderboardUserCache c WHERE LOWER(c.college) LIKE LOWER(CONCAT('%', :college, '%'))")
    Page<LeaderboardUserCache> findByCollegeIncludingMock(@Param("college") String college, Pageable pageable);

    // ── Rank computation queries ───────────────────────────────────────────────

    /**
     * Count how many real-only users have a higher weighted score than the given user (for rank computation).
     */
    @Query("SELECT COUNT(c) FROM LeaderboardUserCache c WHERE c.realDataOnly = true AND c.weightedScore > :score")
    long countRealOnlyWithHigherScore(@Param("score") Double score);

    @Query("SELECT COUNT(c) FROM LeaderboardUserCache c WHERE c.realDataOnly = true AND LOWER(c.college) LIKE LOWER(CONCAT('%', :college, '%')) AND c.weightedScore > :score")
    long countCollegeRealOnlyWithHigherScore(@Param("college") String college, @Param("score") Double score);

    Optional<LeaderboardUserCache> findByUserId(Long userId);

    /**
     * Most recent sync timestamp across all cache entries.
     */
    @Query("SELECT MAX(c.lastSyncedAt) FROM LeaderboardUserCache c")
    java.time.LocalDateTime findLatestSyncedAt();
}
