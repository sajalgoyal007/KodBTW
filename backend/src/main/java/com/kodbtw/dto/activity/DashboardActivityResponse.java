package com.kodbtw.dto.activity;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardActivityResponse(
        LocalDateTime generatedAt,
        boolean available,
        boolean realDataOnly,
        String message,
        List<String> platformsWithRealSnapshots
) {}
