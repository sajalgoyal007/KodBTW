package com.kodbtw.controller;

import com.kodbtw.dto.contest.ContestCalendarResponse;
import com.kodbtw.dto.contest.ContestDto;
import com.kodbtw.dto.contest.ContestSourceStatusDto;
import com.kodbtw.service.ContestCalendarService;
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

    @BeforeEach
    void resetService() {
        reset(service);
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
}
