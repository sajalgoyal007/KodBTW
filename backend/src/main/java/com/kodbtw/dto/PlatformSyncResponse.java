package com.kodbtw.dto;

public record PlatformSyncResponse(
        PlatformSyncStatusResponse syncStatus,
        PlatformStats currentStats,
        boolean cooldownApplied
) {
    public PlatformSyncResponse(PlatformSyncStatusResponse syncStatus, PlatformStats currentStats) {
        this(syncStatus, currentStats, false);
    }
}
