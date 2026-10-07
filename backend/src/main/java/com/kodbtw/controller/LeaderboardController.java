package com.kodbtw.controller;

import com.kodbtw.dto.leaderboard.LeaderboardPageResponse;
import com.kodbtw.dto.leaderboard.MyRankResponse;
import com.kodbtw.entity.User;
import com.kodbtw.service.LeaderboardService;
import com.kodbtw.service.LeaderboardSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Leaderboard REST API.
 *
 * <p>All endpoints require JWT authentication.
 *
 * <p>GET /api/leaderboard         — global paginated leaderboard
 * <p>GET /api/leaderboard/college — college-filtered leaderboard
 * <p>GET /api/leaderboard/me      — current user's rank and metrics
 * <p>POST /api/leaderboard/sync   — manual sync trigger (debug/admin use)
 */
@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final LeaderboardSyncService leaderboardSyncService;

    public LeaderboardController(LeaderboardService leaderboardService,
                                 LeaderboardSyncService leaderboardSyncService) {
        this.leaderboardService = leaderboardService;
        this.leaderboardSyncService = leaderboardSyncService;
    }

    /**
     * Global leaderboard.
     *
     * @param page       zero-based page index (default 0)
     * @param size       page size, max 100 (default 25)
     * @param sort       "score" | "solved" | "rating" (default "score")
     * @param dataFilter "real" | "all" (default "real")
     */
    @GetMapping
    public ResponseEntity<LeaderboardPageResponse> getGlobalLeaderboard(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "25")   int size,
            @RequestParam(defaultValue = "score") String sort,
            @RequestParam(defaultValue = "real")  String dataFilter) {

        LeaderboardPageResponse response = leaderboardService.getGlobalLeaderboard(
                page, size, sort, dataFilter, user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * College-filtered leaderboard.
     *
     * @param college    partial college name (LIKE match, case-insensitive)
     * @param page       zero-based page index (default 0)
     * @param size       page size, max 100 (default 25)
     * @param sort       "score" | "solved" | "rating" (default "score")
     * @param dataFilter "real" | "all" (default "real")
     */
    @GetMapping("/college")
    public ResponseEntity<LeaderboardPageResponse> getCollegeLeaderboard(
            @AuthenticationPrincipal User user,
            @RequestParam String college,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "25")   int size,
            @RequestParam(defaultValue = "score") String sort,
            @RequestParam(defaultValue = "real")  String dataFilter) {

        if (college == null || college.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        LeaderboardPageResponse response = leaderboardService.getCollegeLeaderboard(
                college.trim(), page, size, sort, dataFilter, user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Current user's rank and metrics.
     */
    @GetMapping("/me")
    public ResponseEntity<MyRankResponse> getMyRank(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(leaderboardService.getMyRank(user.getId()));
    }

    /**
     * Manual sync trigger. Intended for debug/admin use only.
     * Runs synchronously — for small user bases only.
     */
    @PostMapping("/sync")
    public ResponseEntity<String> triggerSync(@AuthenticationPrincipal User user) {
        leaderboardSyncService.syncAllUsers();
        return ResponseEntity.ok("{\"message\":\"Leaderboard sync triggered successfully\"}");
    }
}
