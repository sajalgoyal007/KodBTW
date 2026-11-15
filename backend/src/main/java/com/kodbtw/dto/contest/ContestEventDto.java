package com.kodbtw.dto.contest;

import java.time.Instant;

public record ContestEventDto(
        String id,
        String platform,
        String platformDisplayName,
        String title,
        Instant startTimeUtc,
        Instant endTimeUtc,
        long durationSeconds,
        String contestUrl,
        String status
) {
}
