package com.kodbtw.dto.contest;

import java.time.Instant;
import java.util.List;

public record ContestMonthCalendarResponse(
        int year,
        int month,
        String timeZone,
        Instant generatedAt,
        List<ContestEventDto> contests
) {
}
