package com.kodbtw.dto;

public record PlatformSyncResponse(
        PlatformSyncStatusResponse syncStatus,
        PlatformStats currentStats
) { }
