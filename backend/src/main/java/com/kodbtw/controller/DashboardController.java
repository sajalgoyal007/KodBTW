package com.kodbtw.controller;

import com.kodbtw.dto.DashboardStatsResponse;
import com.kodbtw.dto.analytics.DashboardAnalyticsResponse;
import com.kodbtw.dto.history.DashboardHistoryResponse;
import com.kodbtw.dto.insights.DashboardInsightsResponse;
import com.kodbtw.entity.User;
import com.kodbtw.service.DashboardAnalyticsService;
import com.kodbtw.service.DashboardHistoryService;
import com.kodbtw.service.DashboardInsightsService;
import com.kodbtw.service.PlatformStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PlatformStatsService platformStatsService;
    private final DashboardAnalyticsService dashboardAnalyticsService;
    private final DashboardHistoryService dashboardHistoryService;
    private final DashboardInsightsService dashboardInsightsService;

    public DashboardController(PlatformStatsService platformStatsService,
                               DashboardAnalyticsService dashboardAnalyticsService,
                               DashboardHistoryService dashboardHistoryService,
                               DashboardInsightsService dashboardInsightsService) {
        this.platformStatsService = platformStatsService;
        this.dashboardAnalyticsService = dashboardAnalyticsService;
        this.dashboardHistoryService = dashboardHistoryService;
        this.dashboardInsightsService = dashboardInsightsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(platformStatsService.getDashboardStats(user.getId()));
    }

    @GetMapping("/analytics")
    public ResponseEntity<DashboardAnalyticsResponse> getAnalytics(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardAnalyticsService.getAnalytics(user.getId()));
    }

    @GetMapping("/history")
    public ResponseEntity<DashboardHistoryResponse> getHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "30d") String range) {
        return ResponseEntity.ok(dashboardHistoryService.getHistory(user.getId(), range));
    }

    @GetMapping("/insights")
    public ResponseEntity<DashboardInsightsResponse> getInsights(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardInsightsService.getInsights(user.getId()));
    }
}
