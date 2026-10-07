package com.kodbtw.controller;

import com.kodbtw.dto.PlatformAccountRequest;
import com.kodbtw.dto.PlatformAccountResponse;
import com.kodbtw.entity.User;
import com.kodbtw.service.PlatformAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kodbtw.dto.PlatformStats;
import com.kodbtw.service.PlatformStatsService;
import com.kodbtw.dto.PlatformSyncResponse;
import com.kodbtw.dto.PlatformSyncStatusResponse;
import com.kodbtw.service.PlatformSyncService;
import com.kodbtw.service.LeaderboardSyncService;

import java.util.List;

@RestController
@RequestMapping("/api/platform-accounts")
public class PlatformAccountController {

    private final PlatformAccountService platformAccountService;
    private final PlatformStatsService platformStatsService;
    private final PlatformSyncService platformSyncService;
    private final LeaderboardSyncService leaderboardSyncService;

    public PlatformAccountController(PlatformAccountService platformAccountService,
                                     PlatformStatsService platformStatsService,
                                     PlatformSyncService platformSyncService,
                                     LeaderboardSyncService leaderboardSyncService) {
        this.platformAccountService = platformAccountService;
        this.platformStatsService = platformStatsService;
        this.platformSyncService = platformSyncService;
        this.leaderboardSyncService = leaderboardSyncService;
    }

    @PostMapping
    public ResponseEntity<PlatformAccountResponse> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PlatformAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(platformAccountService.create(user.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<PlatformAccountResponse>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(platformAccountService.getAll(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlatformAccountResponse> getOne(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(platformAccountService.getOne(user.getId(), id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<PlatformStats> getStats(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(platformStatsService.getStats(user.getId(), id));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<PlatformSyncResponse> sync(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        PlatformSyncResponse response = platformSyncService.syncOwnedAccount(user.getId(), id);
        leaderboardSyncService.refreshUserCache(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/sync-status")
    public ResponseEntity<PlatformSyncStatusResponse> getSyncStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(platformSyncService.getOwnedStatus(user.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlatformAccountResponse> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody PlatformAccountRequest request) {
        return ResponseEntity.ok(platformAccountService.update(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        platformAccountService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
