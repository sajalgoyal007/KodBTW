package com.kodbtw.dto.contest;

import java.time.Instant;
import java.util.List;

public record ContestCalendarResponse(
        Instant generatedAt,
        boolean stale,
        List<ContestSourceStatusDto> sources,
        List<ContestDto> contests
) {}
