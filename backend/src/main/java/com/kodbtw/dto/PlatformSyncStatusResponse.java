package com.kodbtw.dto;

import com.kodbtw.entity.SyncStatus;

import java.time.LocalDateTime;

public record PlatformSyncStatusResponse(
        SyncStatus status,
        LocalDateTime lastAttemptAt,
        LocalDateTime lastSuccessAt,
        LocalDateTime lastFailureAt,
        String failureCategory,
        String failureMessage,
        boolean fresh
) { }
