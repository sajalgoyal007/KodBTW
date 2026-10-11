package com.kodbtw.dto.contest;

import java.time.Instant;

public record ContestSourceStatusDto(
        String platform,
        String status,
        String message,
        Instant fetchedAt,
        Instant cacheExpiresAt,
        String source
) {}
