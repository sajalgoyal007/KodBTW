package com.kodbtw.controller;

import com.kodbtw.dto.contest.ContestCalendarResponse;
import com.kodbtw.dto.contest.ContestDto;
import com.kodbtw.dto.contest.ContestEventDto;
import com.kodbtw.dto.contest.ContestMonthCalendarResponse;
import com.kodbtw.dto.contest.ContestSourceStatusDto;
import com.kodbtw.service.ContestCalendarService;
import com.kodbtw.service.ContestEventQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ContestCalendarControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private ContestCalendarService service;
    @MockBean private ContestEventQueryService monthCalendarService;

    @BeforeEach
    void resetService() {
        reset(service, monthCalendarService);
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/contests"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser
    void returnsNormalizedContestResponseAndUsesBoundedDefaultLimit() throws Exception {
        Instant start = Instant.parse("2026-10-12T12:00:00Z");
        when(service.getCalendar(50)).thenReturn(new ContestCalendarResponse(
                Instant.parse("2026-10-11T12:00:00Z"), false,
                List.of(new ContestSourceStatusDto("CODEFORCES", "AVAILABLE", null,
                        Instant.parse("2026-10-11T12:00:00Z"), Instant.parse("2026-10-11T12:10:00Z"), "CODEFORCES_API")),
                List.of(new ContestDto("123", "CODEFORCES", "Round", start, 7200,
                        "https://codeforces.com/contest/123", "CODEFORCES_API",
                        Instant.parse("2026-10-11T12:00:00Z")))));

        mockMvc.perform(get("/api/contests").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stale").value(false))
                .andExpect(jsonPath("$.sources[0].platform").value("CODEFORCES"))
                .andExpect(jsonPath("$.contests[0].id").value("123"))
                .andExpect(jsonPath("$.contests[0].startTime").value("2026-10-12T12:00:00Z"))
                .andExpect(jsonPath("$.contests[0].officialUrl").value("https://codeforces.com/contest/123"));
        verify(service).getCalendar(50);
    }

    @Test
    @WithMockUser
    void rejectsUnsupportedPlatformAndUnreasonableLimits() throws Exception {
        mockMvc.perform(get("/api/contests").param("platform", "LEETCODE"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/contests").param("limit", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/contests").param("limit", "101"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void servesTheMonthlyCalendarWithoutAuthentication() throws Exception {
        Instant start = Instant.parse("2026-11-15T14:30:00Z");
        Instant end = Instant.parse("2026-11-15T16:30:00Z");
        when(monthCalendarService.getMonthCalendar(null, null, null)).thenReturn(
                new ContestMonthCalendarResponse(2026, 11, "Asia/Kolkata",
                        Instant.parse("2026-11-01T00:00:00Z"),
                        List.of(new ContestEventDto("7", "LEETCODE", "LeetCode", "Weekly Contest 399",
                                start, end, 7200, "https://leetcode.com/contest/weekly-contest-399",
                                "UPCOMING"))));

        mockMvc.perform(get("/api/contests/calendar").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(11))
                .andExpect(jsonPath("$.timeZone").value("Asia/Kolkata"))
                .andExpect(jsonPath("$.contests[0].id").value("7"))
                .andExpect(jsonPath("$.contests[0].platform").value("LEETCODE"))
                .andExpect(jsonPath("$.contests[0].platformDisplayName").value("LeetCode"))
                .andExpect(jsonPath("$.contests[0].title").value("Weekly Contest 399"))
                .andExpect(jsonPath("$.contests[0].startTimeUtc").value("2026-11-15T14:30:00Z"))
                .andExpect(jsonPath("$.contests[0].endTimeUtc").value("2026-11-15T16:30:00Z"))
                .andExpect(jsonPath("$.contests[0].durationSeconds").value(7200))
                .andExpect(jsonPath("$.contests[0].status").value("UPCOMING"))
                .andExpect(jsonPath("$.contests[0].contestUrl")
                        .value("https://leetcode.com/contest/weekly-contest-399"));
        verify(monthCalendarService).getMonthCalendar(null, null, null);
    }

    @Test
    @WithMockUser
    void passesValidFilterParametersThrough() throws Exception {
        when(monthCalendarService.getMonthCalendar(2026, 11, List.of("CODEFORCES", "LEETCODE")))
                .thenReturn(new ContestMonthCalendarResponse(2026, 11, "Asia/Kolkata",
                        Instant.parse("2026-11-01T00:00:00Z"), List.of()));

        mockMvc.perform(get("/api/contests/calendar")
                        .param("year", "2026")
                        .param("month", "11")
                        .param("platforms", "CODEFORCES", "LEETCODE"))
                .andExpect(status().isOk());

        verify(monthCalendarService).getMonthCalendar(2026, 11, List.of("CODEFORCES", "LEETCODE"));
    }

    @Test
    void rejectsInvalidCalendarParametersBeforeHittingTheService() throws Exception {
        mockMvc.perform(get("/api/contests/calendar").param("month", "13"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/contests/calendar").param("month", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/contests/calendar").param("year", "1999"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/contests/calendar").param("platforms", "HACKERX"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(monthCalendarService);
    }
}
