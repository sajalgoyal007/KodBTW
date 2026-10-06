package com.kodbtw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodbtw.dto.LoginRequest;
import com.kodbtw.dto.PlatformAccountRequest;
import com.kodbtw.dto.RegisterRequest;
import com.kodbtw.entity.Platform;
import com.kodbtw.repository.PlatformAccountRepository;
import com.kodbtw.repository.ProfileRepository;
import com.kodbtw.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PlatformAccountRepository platformAccountRepository;

    @MockBean
    private com.kodbtw.adapter.leetcode.LeetCodeClient leetCodeClient;

    @MockBean
    private com.kodbtw.adapter.codeforces.CodeforcesClient codeforcesClient;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        platformAccountRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();

        userToken = registerAndLogin("dashboarduser@example.com", "password123");
    }

    private String registerAndLogin(String email, String password) throws Exception {
        RegisterRequest register = new RegisterRequest("Dashboard User", email, password);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test
    void getStats_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getStats_authenticated_whenNoAccounts_returns200AndEmptyState() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overview.connectedPlatformsCount", is(0)))
                .andExpect(jsonPath("$.overview.totalProblemsSolved", is(0)))
                .andExpect(jsonPath("$.overview.easySolved", is(0)))
                .andExpect(jsonPath("$.overview.mediumSolved", is(0)))
                .andExpect(jsonPath("$.overview.hardSolved", is(0)))
                .andExpect(jsonPath("$.overview.contestsParticipated", is(0)))
                .andExpect(jsonPath("$.overview.currentStreak", nullValue()))
                .andExpect(jsonPath("$.overview.longestStreak", nullValue()))
                .andExpect(jsonPath("$.platforms", hasSize(0)));
    }

    @Test
    void getStats_authenticated_withLinkedAccount_returns200AndAggregatedStats() throws Exception {
        // Link a CodeChef account (mock adapter)
        PlatformAccountRequest accountRequest = new PlatformAccountRequest();
        accountRequest.setPlatform(Platform.CODECHEF);
        accountRequest.setUsername("testchef");

        mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/dashboard/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overview.connectedPlatformsCount", is(1)))
                .andExpect(jsonPath("$.overview.totalProblemsSolved", is(210)))
                .andExpect(jsonPath("$.overview.easySolved", is(120)))
                .andExpect(jsonPath("$.overview.mediumSolved", is(70)))
                .andExpect(jsonPath("$.overview.hardSolved", is(20)))
                .andExpect(jsonPath("$.overview.contestsParticipated", is(25)))
                .andExpect(jsonPath("$.platforms", hasSize(1)))
                .andExpect(jsonPath("$.platforms[0].platform", is("CODECHEF")))
                .andExpect(jsonPath("$.platforms[0].username", is("testchef")))
                .andExpect(jsonPath("$.platforms[0].rating", is(1750)))
                .andExpect(jsonPath("$.platforms[0].rank", is(4500)))
                .andExpect(jsonPath("$.platforms[0].source", is("MOCK")));
    }
}
