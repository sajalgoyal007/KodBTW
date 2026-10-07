package com.kodbtw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodbtw.dto.LoginRequest;
import com.kodbtw.dto.PlatformAccountRequest;
import com.kodbtw.dto.RegisterRequest;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformStatSnapshot;
import com.kodbtw.entity.User;
import com.kodbtw.repository.PlatformAccountRepository;
import com.kodbtw.repository.PlatformStatSnapshotRepository;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verifyNoInteractions;

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

    @Autowired
    private com.kodbtw.repository.LeaderboardUserCacheRepository leaderboardUserCacheRepository;

    @Autowired
    private PlatformStatSnapshotRepository platformStatSnapshotRepository;

    @MockBean
    private com.kodbtw.adapter.leetcode.LeetCodeClient leetCodeClient;

    @MockBean
    private com.kodbtw.adapter.codeforces.CodeforcesClient codeforcesClient;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        platformStatSnapshotRepository.deleteAll();
        leaderboardUserCacheRepository.deleteAll();
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

    private void refreshLinkedAccounts() throws Exception {
        MvcResult list = mockMvc.perform(get("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andReturn();
        for (var account : objectMapper.readTree(list.getResponse().getContentAsString())) {
            mockMvc.perform(post("/api/platform-accounts/" + account.get("id").asLong() + "/sync")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)).andExpect(status().isOk());
        }
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

        refreshLinkedAccounts();

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

    @Test
    void getAnalytics_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard/analytics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAnalytics_authenticated_whenNoAccounts_returns200AndEmptyState() throws Exception {
        mockMvc.perform(get("/api/dashboard/analytics")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty.totalProblemsSolved", is(0)))
                .andExpect(jsonPath("$.difficulty.easy.count", is(0)))
                .andExpect(jsonPath("$.difficulty.easy.percentage", is(0.0)))
                .andExpect(jsonPath("$.difficulty.medium.count", is(0)))
                .andExpect(jsonPath("$.difficulty.medium.percentage", is(0.0)))
                .andExpect(jsonPath("$.difficulty.hard.count", is(0)))
                .andExpect(jsonPath("$.difficulty.hard.percentage", is(0.0)))
                .andExpect(jsonPath("$.difficulty.platformBreakdown", hasSize(0)))
                .andExpect(jsonPath("$.platformComparison", hasSize(0)))
                .andExpect(jsonPath("$.contests.totalContests", is(0)))
                .andExpect(jsonPath("$.contests.platformBreakdown", hasSize(0)));
    }

    @Test
    void getAnalytics_authenticated_withLinkedAccount_returns200AndAnalytics() throws Exception {
        PlatformAccountRequest accountRequest = new PlatformAccountRequest();
        accountRequest.setPlatform(Platform.CODECHEF);
        accountRequest.setUsername("testchef");

        mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated());

        refreshLinkedAccounts();

        mockMvc.perform(get("/api/dashboard/analytics")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty.totalProblemsSolved", is(210)))
                .andExpect(jsonPath("$.difficulty.easy.count", is(120)))
                .andExpect(jsonPath("$.difficulty.easy.percentage", is(57.14)))
                .andExpect(jsonPath("$.difficulty.medium.count", is(70)))
                .andExpect(jsonPath("$.difficulty.medium.percentage", is(33.33)))
                .andExpect(jsonPath("$.difficulty.hard.count", is(20)))
                .andExpect(jsonPath("$.difficulty.hard.percentage", is(9.52)))
                .andExpect(jsonPath("$.difficulty.platformBreakdown", hasSize(1)))
                .andExpect(jsonPath("$.difficulty.platformBreakdown[0].platform", is("CODECHEF")))
                .andExpect(jsonPath("$.difficulty.platformBreakdown[0].easy", is(120)))
                .andExpect(jsonPath("$.difficulty.platformBreakdown[0].medium", is(70)))
                .andExpect(jsonPath("$.difficulty.platformBreakdown[0].hard", is(20)))
                .andExpect(jsonPath("$.difficulty.platformBreakdown[0].total", is(210)))
                .andExpect(jsonPath("$.platformComparison", hasSize(1)))
                .andExpect(jsonPath("$.platformComparison[0].platform", is("CODECHEF")))
                .andExpect(jsonPath("$.platformComparison[0].username", is("testchef")))
                .andExpect(jsonPath("$.platformComparison[0].totalSolved", is(210)))
                .andExpect(jsonPath("$.platformComparison[0].sharePercentage", is(100.0)))
                .andExpect(jsonPath("$.platformComparison[0].rating", is(1750)))
                .andExpect(jsonPath("$.platformComparison[0].rank", is(4500)))
                .andExpect(jsonPath("$.platformComparison[0].contestsParticipated", is(25)))
                .andExpect(jsonPath("$.platformComparison[0].source", is("MOCK")))
                .andExpect(jsonPath("$.contests.totalContests", is(25)))
                .andExpect(jsonPath("$.contests.platformBreakdown", hasSize(1)))
                .andExpect(jsonPath("$.contests.platformBreakdown[0].platform", is("CODECHEF")))
                .andExpect(jsonPath("$.contests.platformBreakdown[0].contests", is(25)))
                .andExpect(jsonPath("$.contests.platformBreakdown[0].rating", is(1750)))
                .andExpect(jsonPath("$.contests.platformBreakdown[0].rank", is(4500)));
    }

    @Test
    void getHistory_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getHistory_authenticatedDefaultsToThirtyDaysAndReturnsEmptyHistory() throws Exception {
        mockMvc.perform(get("/api/dashboard/history")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range", is("30d")))
                .andExpect(jsonPath("$.snapshotDates", hasSize(0)))
                .andExpect(jsonPath("$.overallSolved", hasSize(0)))
                .andExpect(jsonPath("$.platforms", hasSize(0)))
                .andExpect(jsonPath("$.freshness.snapshotCount", is(0)));
    }

    @Test
    void getHistory_invalidRange_returns400() throws Exception {
        mockMvc.perform(get("/api/dashboard/history")
                        .param("range", "all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInsights_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard/insights"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getInsights_returnsOnlyAuthenticatedUsersRealSnapshotWithoutProviderCalls() throws Exception {
        User owner = userRepository.findByEmail("dashboarduser@example.com").orElseThrow();
        registerAndLogin("otherinsights@example.com", "password123");
        User otherUser = userRepository.findByEmail("otherinsights@example.com").orElseThrow();

        saveSnapshot(owner, "LEETCODE", "LEETCODE_REAL", 291, 124, 129, 38, 1352);
        saveSnapshot(owner, "CODECHEF", "CODECHEF_MOCK", 900, 400, 300, 200, 1700);
        saveSnapshot(otherUser, "CODEFORCES", "CODEFORCES_REAL", 600, null, null, null, 1850);

        mockMvc.perform(get("/api/dashboard/insights")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataAvailability.snapshotCount", is(1)))
                .andExpect(jsonPath("$.dataAvailability.platformCount", is(1)))
                .andExpect(jsonPath("$.dataAvailability.realDataOnly", is(true)))
                .andExpect(jsonPath("$.dataAvailability.hasHistoricalComparison", is(false)))
                .andExpect(jsonPath("$.dataAvailability.latestSnapshotDate").exists())
                .andExpect(jsonPath("$.summary").value(org.hamcrest.Matchers.containsString("Current verified baseline: 291")))
                .andExpect(jsonPath("$.platformInsights", hasSize(1)))
                .andExpect(jsonPath("$.platformInsights[0].platform", is("LEETCODE")))
                .andExpect(jsonPath("$.progressInsights[0].type", is("PROGRESS_BASELINE")))
                .andExpect(jsonPath("$.ratingInsights[0].type", is("RATING_BASELINE")))
                .andExpect(jsonPath("$.streakInsights", hasSize(0)));

        verifyNoInteractions(leetCodeClient, codeforcesClient);
    }

    private void saveSnapshot(User user, String platform, String source, int total,
                              Integer easy, Integer medium, Integer hard, Integer rating) {
        PlatformStatSnapshot snapshot = new PlatformStatSnapshot();
        snapshot.setUser(user);
        snapshot.setPlatform(platform);
        snapshot.setSnapshotDate(LocalDate.now());
        snapshot.setTotalSolved(total);
        snapshot.setEasySolved(easy);
        snapshot.setMediumSolved(medium);
        snapshot.setHardSolved(hard);
        snapshot.setRating(rating);
        snapshot.setSource(source);
        platformStatSnapshotRepository.save(snapshot);
    }
}
