package com.kodbtw.controller;

import com.kodbtw.dto.contest.ContestCalendarResponse;
import com.kodbtw.service.ContestCalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contests")
public class ContestCalendarController {
    private static final int MAX_LIMIT = 100;
    private final ContestCalendarService contestCalendarService;

    public ContestCalendarController(ContestCalendarService contestCalendarService) {
        this.contestCalendarService = contestCalendarService;
    }

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
}
