package com.kodbtw.controller;

import com.kodbtw.dto.DashboardStatsResponse;
import com.kodbtw.dto.analytics.DashboardAnalyticsResponse;
import com.kodbtw.entity.User;
import com.kodbtw.service.DashboardAnalyticsService;
import com.kodbtw.service.PlatformStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PlatformStatsService platformStatsService;
    private final DashboardAnalyticsService dashboardAnalyticsService;

    public DashboardController(PlatformStatsService platformStatsService,
                               DashboardAnalyticsService dashboardAnalyticsService) {
        this.platformStatsService = platformStatsService;
        this.dashboardAnalyticsService = dashboardAnalyticsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(platformStatsService.getDashboardStats(user.getId()));
    }

    @GetMapping("/analytics")
    public ResponseEntity<DashboardAnalyticsResponse> getAnalytics(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardAnalyticsService.getAnalytics(user.getId()));
    }
}
