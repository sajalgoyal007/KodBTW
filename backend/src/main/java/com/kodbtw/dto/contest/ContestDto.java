package com.kodbtw.dto.contest;

import java.time.Instant;

public record ContestDto(
        String id,
        String platform,
        String name,
        Instant startTime,
        long durationSeconds,
        String officialUrl,
        String source,
        Instant fetchedAt
) {}
