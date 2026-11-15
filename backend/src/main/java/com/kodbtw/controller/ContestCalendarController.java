package com.kodbtw.controller;

import com.kodbtw.contest.ContestPlatform;
import com.kodbtw.dto.contest.ContestCalendarResponse;
import com.kodbtw.dto.contest.ContestMonthCalendarResponse;
import com.kodbtw.service.ContestCalendarService;
import com.kodbtw.service.ContestEventQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contests")
public class ContestCalendarController {
    private static final int MAX_LIMIT = 100;
    private static final int MIN_YEAR = 2000;
    private static final int MAX_YEAR = 2100;

    private final ContestCalendarService contestCalendarService;
    private final ContestEventQueryService contestEventQueryService;

    public ContestCalendarController(ContestCalendarService contestCalendarService,
            ContestEventQueryService contestEventQueryService) {
        this.contestCalendarService = contestCalendarService;
        this.contestEventQueryService = contestEventQueryService;
    }

    /**
     * Legacy live-provider schedule endpoint. Kept unchanged for backward
     * compatibility; still requires authentication.
     */
    @GetMapping
    public ResponseEntity<ContestCalendarResponse> getContests(
            @RequestParam(required = false) String platform,
            @RequestParam(defaultValue = "50") int limit) {
        if (platform != null && !platform.isBlank()
                && !platform.equalsIgnoreCase("ALL")
                && !platform.equalsIgnoreCase("CODEFORCES")) {
            throw new IllegalArgumentException("This contest platform is not available.");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Contest limit must be between 1 and 100.");
        }
        return ResponseEntity.ok(contestCalendarService.getCalendar(limit));
    }

    /**
     * Public multi-platform monthly calendar backed by {@code contest_events}.
     * Defaults to the current IST month and all eight supported platforms.
     */
    @GetMapping("/calendar")
    public ResponseEntity<ContestMonthCalendarResponse> getMonthlyCalendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) List<String> platforms) {
        validateYear(year);
        validateMonth(month);
        validatePlatforms(platforms);
        return ResponseEntity.ok(contestEventQueryService.getMonthCalendar(year, month, platforms));
    }

    private void validateYear(Integer year) {
        if (year != null && (year < MIN_YEAR || year > MAX_YEAR)) {
            throw new IllegalArgumentException(
                    "Contest calendar year must be between " + MIN_YEAR + " and " + MAX_YEAR + ".");
        }
    }

    private void validateMonth(Integer month) {
        if (month != null && (month < 1 || month > 12)) {
            throw new IllegalArgumentException("Contest calendar month must be between 1 and 12.");
        }
    }

    private void validatePlatforms(List<String> platforms) {
        if (platforms == null) {
            return;
        }
        for (String value : platforms) {
            if (value == null) {
                continue;
            }
            for (String token : value.split(",")) {
                String candidate = token.trim();
                if (candidate.isEmpty() || candidate.equalsIgnoreCase("ALL")) {
                    continue;
                }
                if (ContestPlatform.fromCode(candidate).isEmpty()) {
                    throw new IllegalArgumentException("Unsupported contest platform: " + candidate);
                }
            }
        }
    }
}
