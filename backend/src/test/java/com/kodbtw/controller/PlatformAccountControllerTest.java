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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlatformAccountControllerTest {

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
    private com.kodbtw.repository.PlatformStatSnapshotRepository platformStatSnapshotRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.kodbtw.adapter.leetcode.LeetCodeClient leetCodeClient;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.kodbtw.adapter.codeforces.CodeforcesClient codeforcesClient;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.kodbtw.adapter.codechef.CodeChefClient codeChefClient;

    private String userToken;
    private String otherUserToken;

    @BeforeEach
    void setUp() throws Exception {
        platformStatSnapshotRepository.deleteAll();
        leaderboardUserCacheRepository.deleteAll();
        platformAccountRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();

        userToken = registerAndLogin("sajal@example.com", "password123");
        otherUserToken = registerAndLogin("other@example.com", "password456");
    }

    private String registerAndLogin(String email, String password) throws Exception {
        RegisterRequest register = new RegisterRequest("Test User", email, password);
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

    private PlatformAccountRequest makeRequest(Platform platform, String username, String url) {
        PlatformAccountRequest r = new PlatformAccountRequest();
        r.setPlatform(platform);
        r.setUsername(username);
        r.setProfileUrl(url);
        return r;
    }

    @Test
    void testUnauthenticatedCreateReturns401() throws Exception {
        mockMvc.perform(post("/api/platform-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                makeRequest(Platform.LEETCODE, "testuser", null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUnauthenticatedGetAllReturns401() throws Exception {
        mockMvc.perform(get("/api/platform-accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreatePlatformAccount() throws Exception {
        mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                makeRequest(Platform.LEETCODE, "sajal007", "https://leetcode.com/u/sajal007/"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.platform").value("LEETCODE"))
                .andExpect(jsonPath("$.username").value("sajal007"))
                .andExpect(jsonPath("$.verified").value(false))
                .andExpect(jsonPath("$.sourceStatus").value("REAL_AVAILABLE"));
    }

    @Test
    void allFivePlatformsCanConnectAndPendingSourcesHaveNoFakeStats() throws Exception {
        for (Platform platform : Platform.values()) {
            MvcResult result = mockMvc.perform(post("/api/platform-accounts")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(makeRequest(platform, "same-handle", null))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.platform").value(platform.name()))
                    .andReturn();
            long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
            String expectedStatus = platform.hasLiveStatsSource() ? "REAL_AVAILABLE" : "SOURCE_PENDING";
            mockMvc.perform(get("/api/platform-accounts/" + id)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sourceStatus").value(expectedStatus));
            if (!platform.hasLiveStatsSource()) {
                mockMvc.perform(get("/api/platform-accounts/" + id + "/stats")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.source").value("SOURCE_PENDING"))
                        .andExpect(jsonPath("$.totalProblemsSolved").doesNotExist())
                        .andExpect(jsonPath("$.rating").doesNotExist());
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(platformStatSnapshotRepository.findAll().isEmpty());
        org.mockito.Mockito.verifyNoInteractions(leetCodeClient, codeforcesClient);
    }

    @Test
    void testGetAllPlatformAccounts() throws Exception {
        mockMvc.perform(post("/api/platform-accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(makeRequest(Platform.LEETCODE, "user1", null))));

        mockMvc.perform(post("/api/platform-accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(makeRequest(Platform.CODEFORCES, "user1cf", null))));

        mockMvc.perform(get("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetOnePlatformAccount() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRequest(Platform.CODECHEF, "chefuser", null))))
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(get("/api/platform-accounts/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platform").value("CODECHEF"))
                .andExpect(jsonPath("$.username").value("chefuser"));
    }

    @Test
    void testUpdatePlatformAccount() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRequest(Platform.HACKERRANK, "oldname", null))))
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(put("/api/platform-accounts/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                makeRequest(Platform.HACKERRANK, "newname", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newname"));
    }

    @Test
    void testDeletePlatformAccount() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRequest(Platform.GEEKSFORGEEKS, "gfguser", null))))
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(delete("/api/platform-accounts/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/platform-accounts/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void onlyOwnerCanDisconnectAndOtherAccountsRemain() throws Exception {
        MvcResult first = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRequest(Platform.CODECHEF, "chef", null))))
                .andExpect(status().isCreated()).andReturn();
        long firstId = objectMapper.readTree(first.getResponse().getContentAsString()).get("id").asLong();
        MvcResult second = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRequest(Platform.HACKERRANK, "chef", null))))
                .andExpect(status().isCreated()).andReturn();
        long secondId = objectMapper.readTree(second.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/platform-accounts/" + firstId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/platform-accounts/" + firstId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/platform-accounts/" + secondId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void testDuplicatePlatformReturns409() throws Exception {
        mockMvc.perform(post("/api/platform-accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(makeRequest(Platform.LEETCODE, "first", null))));

        mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRequest(Platform.LEETCODE, "second", null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void testUserCannotAccessOtherUserAccount() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRequest(Platform.CODEFORCES, "owner", null))))
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Other user tries to access it — should get 404 (safe; no information leakage)
        mockMvc.perform(get("/api/platform-accounts/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testValidationFailureMissingUsername() throws Exception {
        PlatformAccountRequest request = new PlatformAccountRequest();
        request.setPlatform(Platform.LEETCODE);
        request.setUsername("");

        mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void testValidationFailureMissingPlatform() throws Exception {
        PlatformAccountRequest request = new PlatformAccountRequest();
        request.setUsername("someuser");

        mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void testGetStatsUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/platform-accounts/1/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetStatsSuccessReturnsRealLeetCodeStats() throws Exception {
        com.kodbtw.adapter.leetcode.dto.LeetCodeDto.GraphQLData mockData =
                new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.GraphQLData(
                        new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.MatchedUser(
                                "sajal",
                                new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.UserProfile(100000),
                                new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.SubmitStatsGlobal(java.util.List.of(
                                        new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.SubmissionCount("All", 350),
                                        new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.SubmissionCount("Easy", 180),
                                        new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.SubmissionCount("Medium", 130),
                                        new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.SubmissionCount("Hard", 40)
                                )),
                                new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.UserCalendar(7, 30)
                        ),
                        new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.UserContestRanking(1850.4, 12, 5000)
                );
        org.mockito.Mockito.when(leetCodeClient.fetchUserProfile("sajal")).thenReturn(mockData);

        // Create LeetCode account
        PlatformAccountRequest request = makeRequest(Platform.LEETCODE, "sajal", null);
        MvcResult createResult = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(get("/api/platform-accounts/" + id + "/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("UNSYNCED"))
                .andExpect(jsonPath("$.totalProblemsSolved").doesNotExist());

        mockMvc.perform(post("/api/platform-accounts/" + id + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncStatus.status").value("SUCCEEDED"));

        // Fetch stats
        mockMvc.perform(get("/api/platform-accounts/" + id + "/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platform").value("LEETCODE"))
                .andExpect(jsonPath("$.username").value("sajal"))
                .andExpect(jsonPath("$.profileUrl").value("https://leetcode.com/u/sajal/"))
                .andExpect(jsonPath("$.totalProblemsSolved").value(350))
                .andExpect(jsonPath("$.easySolved").value(180))
                .andExpect(jsonPath("$.mediumSolved").value(130))
                .andExpect(jsonPath("$.hardSolved").value(40))
                .andExpect(jsonPath("$.rating").value(1850))
                .andExpect(jsonPath("$.rank").value(100000))
                .andExpect(jsonPath("$.contestsParticipated").value(12))
                .andExpect(jsonPath("$.currentStreak").value(7))
                .andExpect(jsonPath("$.longestStreak").doesNotExist())
                .andExpect(jsonPath("$.source").value("LEETCODE_REAL"));

        mockMvc.perform(get("/api/dashboard/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overview.totalProblemsSolved").value(350))
                .andExpect(jsonPath("$.overview.easySolved").value(180))
                .andExpect(jsonPath("$.platforms[0].source").value("LEETCODE_REAL"));
    }

    @Test
    void testGetStatsLeetCodeUserNotFoundReturns404() throws Exception {
        org.mockito.Mockito.when(leetCodeClient.fetchUserProfile("missing_user"))
                .thenThrow(new com.kodbtw.exception.ResourceNotFoundException("LeetCode user not found: missing_user"));

        PlatformAccountRequest request = makeRequest(Platform.LEETCODE, "missing_user", null);
        MvcResult createResult = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(post("/api/platform-accounts/" + id + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncStatus.status").value("FAILED"))
                .andExpect(jsonPath("$.syncStatus.failureCategory").value("ACCOUNT_NOT_FOUND"))
                .andExpect(jsonPath("$.syncStatus.failureMessage").exists())
                .andExpect(jsonPath("$.currentStats.source").value("UNSYNCED"));
    }

    @Test
    void testGetStatsLeetCodeExternalErrorReturns502() throws Exception {
        org.mockito.Mockito.when(leetCodeClient.fetchUserProfile("error_user"))
                .thenThrow(new com.kodbtw.exception.PlatformApiException("LeetCode API timeout"));

        PlatformAccountRequest request = makeRequest(Platform.LEETCODE, "error_user", null);
        MvcResult createResult = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(post("/api/platform-accounts/" + id + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncStatus.status").value("FAILED"))
                .andExpect(jsonPath("$.syncStatus.failureCategory").value("TIMEOUT"))
                .andExpect(jsonPath("$.syncStatus.failureMessage").value(org.hamcrest.Matchers.not("LeetCode API timeout")));
    }

    @Test
    void testGetStatsCodeforcesReturnsRealStats() throws Exception {
        com.kodbtw.adapter.codeforces.dto.CodeforcesDto.UserInfo mockInfo =
                new com.kodbtw.adapter.codeforces.dto.CodeforcesDto.UserInfo(
                        "tourist", 3384, 4009, "legendary grandmaster", "tourist",
                        112, 91104, 1265987288L
                );
        org.mockito.Mockito.when(codeforcesClient.fetchUserInfo("tourist")).thenReturn(mockInfo);

        PlatformAccountRequest request = makeRequest(Platform.CODEFORCES, "tourist", null);
        MvcResult createResult = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(post("/api/platform-accounts/" + id + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/platform-accounts/" + id + "/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platform").value("CODEFORCES"))
                .andExpect(jsonPath("$.username").value("tourist"))
                .andExpect(jsonPath("$.profileUrl").value("https://codeforces.com/profile/tourist"))
                .andExpect(jsonPath("$.rating").value(3384))
                .andExpect(jsonPath("$.maxRating").value(4009))
                .andExpect(jsonPath("$.rank").doesNotExist())
                .andExpect(jsonPath("$.totalProblemsSolved").doesNotExist())
                .andExpect(jsonPath("$.easySolved").doesNotExist())
                .andExpect(jsonPath("$.mediumSolved").doesNotExist())
                .andExpect(jsonPath("$.hardSolved").doesNotExist())
                .andExpect(jsonPath("$.contestsParticipated").doesNotExist())
                .andExpect(jsonPath("$.currentStreak").doesNotExist())
                .andExpect(jsonPath("$.longestStreak").doesNotExist())
                .andExpect(jsonPath("$.source").value("CODEFORCES_REAL"));
        org.junit.jupiter.api.Assertions.assertEquals(4009,
                platformStatSnapshotRepository.findAll().getFirst().getMaxRating());
    }

    @Test
    void testGetStatsCodeforcesUserNotFoundReturns404() throws Exception {
        org.mockito.Mockito.when(codeforcesClient.fetchUserInfo("cf_missing"))
                .thenThrow(new com.kodbtw.exception.ResourceNotFoundException("Codeforces user not found: cf_missing"));

        PlatformAccountRequest request = makeRequest(Platform.CODEFORCES, "cf_missing", null);
        MvcResult createResult = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(post("/api/platform-accounts/" + id + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncStatus.status").value("FAILED"))
                .andExpect(jsonPath("$.syncStatus.failureCategory").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void testGetStatsCodeforcesUpstreamFailureReturns502() throws Exception {
        org.mockito.Mockito.when(codeforcesClient.fetchUserInfo("cf_error_user"))
                .thenThrow(new com.kodbtw.exception.PlatformApiException("Codeforces API timeout"));

        PlatformAccountRequest request = makeRequest(Platform.CODEFORCES, "cf_error_user", null);
        MvcResult createResult = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(post("/api/platform-accounts/" + id + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncStatus.status").value("FAILED"))
                .andExpect(jsonPath("$.syncStatus.failureCategory").value("TIMEOUT"));
    }

    @Test
    void testGetStatsOtherUsersAccountReturns404() throws Exception {
        // User 1 creates an account
        PlatformAccountRequest request = makeRequest(Platform.LEETCODE, "user1_lc", null);
        MvcResult createResult = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // User 2 tries to fetch user 1's stats -> 404
        mockMvc.perform(get("/api/platform-accounts/" + id + "/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetStatsNonExistentAccountReturns404() throws Exception {
        mockMvc.perform(get("/api/platform-accounts/99999/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void syncAndStatusRequireAuthenticationAndEnforceOwnership() throws Exception {
        mockMvc.perform(post("/api/platform-accounts/1/sync")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/platform-accounts/1/sync-status")).andExpect(status().isUnauthorized());
        MvcResult created = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRequest(Platform.CODECHEF, "syncuser", null))))
                .andExpect(status().isCreated()).andReturn();
        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(get("/api/platform-accounts/" + id + "/sync-status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/platform-accounts/" + id + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/platform-accounts/" + id + "/sync-status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEVER_SYNCED"));
    }

    @Test
    void syncSuccessIsIdempotentAndFailurePreservesLastSuccessfulStats() throws Exception {
        var mockData = new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.GraphQLData(
                new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.MatchedUser(
                        "historyless", new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.UserProfile(1200),
                        new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.SubmitStatsGlobal(java.util.List.of(
                                new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.SubmissionCount("All", 83))),
                        new com.kodbtw.adapter.leetcode.dto.LeetCodeDto.UserCalendar(2, 10)), null);
        org.mockito.Mockito.when(leetCodeClient.fetchUserProfile("historyless")).thenReturn(mockData);
        MvcResult created = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRequest(Platform.LEETCODE, "historyless", null))))
                .andExpect(status().isCreated()).andReturn();
        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/platform-accounts/" + id + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncStatus.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.cooldownApplied").value(false))
                .andExpect(jsonPath("$.currentStats.totalProblemsSolved").value(83));
        mockMvc.perform(post("/api/platform-accounts/" + id + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cooldownApplied").value(true))
                .andExpect(jsonPath("$.currentStats.totalProblemsSolved").value(83));
        org.junit.jupiter.api.Assertions.assertEquals(1, platformStatSnapshotRepository.findAll().size());

        var accountBeforeRetry = platformAccountRepository.findById(id).orElseThrow();
        accountBeforeRetry.setLastSuccessAt(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC)
                .minusMinutes(16).truncatedTo(java.time.temporal.ChronoUnit.MICROS));
        platformAccountRepository.save(accountBeforeRetry);
        var lastSuccess = accountBeforeRetry.getLastSuccessAt();

        org.mockito.Mockito.when(leetCodeClient.fetchUserProfile("historyless"))
                .thenThrow(new com.kodbtw.exception.PlatformApiException("timeout; upstream-secret"));
        MvcResult failed = mockMvc.perform(post("/api/platform-accounts/" + id + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncStatus.status").value("FAILED"))
                .andExpect(jsonPath("$.syncStatus.failureCategory").value("TIMEOUT"))
                .andExpect(jsonPath("$.currentStats.totalProblemsSolved").value(83))
                .andReturn();
        org.junit.jupiter.api.Assertions.assertFalse(failed.getResponse().getContentAsString().contains("upstream-secret"));
        var account = platformAccountRepository.findById(id).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(lastSuccess, account.getLastSuccessAt());
        org.junit.jupiter.api.Assertions.assertEquals(1, platformStatSnapshotRepository.findAll().size());
    }

    @Test
    void codeChefSyncReturnsTheThirdPartyStatsPersistedInItsSnapshot() throws Exception {
        String username = "sajalgoyal2007";
        var profile = new com.kodbtw.adapter.codechef.dto.CodeChefApiResponse.Profile(
                "https://www.codechef.com/users/" + username, "Test User", 1247, 1519,
                null, "India", 76970, 73803, "1★", 656);
        org.mockito.Mockito.when(codeChefClient.fetchProfile(username)).thenReturn(
                new com.kodbtw.adapter.codechef.dto.CodeChefApiResponse(
                        true, 200, username, profile, "OK", "CodeChef"));

        MvcResult created = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRequest(Platform.CODECHEF, username, null))))
                .andExpect(status().isCreated()).andReturn();
        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/platform-accounts/" + id + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncStatus.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.currentStats.source").value("CODECHEF_THIRD_PARTY"))
                .andExpect(jsonPath("$.currentStats.totalProblemsSolved").value(656))
                .andExpect(jsonPath("$.currentStats.rating").value(1247))
                .andExpect(jsonPath("$.currentStats.maxRating").value(1519))
                .andExpect(jsonPath("$.currentStats.rank").value(76970))
                .andExpect(jsonPath("$.currentStats.lastSyncedAt").isNotEmpty())
                .andExpect(jsonPath("$.currentStats.easySolved").doesNotExist())
                .andExpect(jsonPath("$.currentStats.contestsParticipated").doesNotExist());

        var snapshot = platformStatSnapshotRepository.findAll().get(0);
        org.junit.jupiter.api.Assertions.assertEquals("CODECHEF_THIRD_PARTY", snapshot.getSource());
        org.junit.jupiter.api.Assertions.assertEquals(656, snapshot.getTotalSolved());
        org.junit.jupiter.api.Assertions.assertEquals(1247, snapshot.getRating());
        org.junit.jupiter.api.Assertions.assertNotNull(snapshot.getStatsLastSyncedAt());

        mockMvc.perform(get("/api/platform-accounts/" + id + "/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("CODECHEF_THIRD_PARTY"))
                .andExpect(jsonPath("$.totalProblemsSolved").value(656));
    }

    @Test
    void unsupportedMockPlatformReturnsUnavailableWithoutCreatingFakeSnapshot() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRequest(Platform.GEEKSFORGEEKS, "chefuser", null))))
                .andExpect(status().isCreated()).andReturn();
        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/platform-accounts/" + id + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncStatus.status").value("NEVER_SYNCED"))
                .andExpect(jsonPath("$.currentStats.source").value("SOURCE_PENDING"))
                .andExpect(jsonPath("$.currentStats.totalProblemsSolved").doesNotExist());

        org.junit.jupiter.api.Assertions.assertTrue(platformStatSnapshotRepository.findAll().isEmpty());
    }
}
