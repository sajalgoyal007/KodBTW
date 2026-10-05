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

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.kodbtw.adapter.leetcode.LeetCodeClient leetCodeClient;

    private String userToken;
    private String otherUserToken;

    @BeforeEach
    void setUp() throws Exception {
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
                .andExpect(jsonPath("$.verified").value(false));
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

        mockMvc.perform(get("/api/platform-accounts/" + id + "/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
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

        mockMvc.perform(get("/api/platform-accounts/" + id + "/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Bad Gateway"));
    }

    @Test
    void testGetStatsCodeforcesReturnsMockStats() throws Exception {
        PlatformAccountRequest request = makeRequest(Platform.CODEFORCES, "tourist", null);
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
                .andExpect(jsonPath("$.platform").value("CODEFORCES"))
                .andExpect(jsonPath("$.username").value("tourist"))
                .andExpect(jsonPath("$.rating").value(1450))
                .andExpect(jsonPath("$.rank").value(25000))
                .andExpect(jsonPath("$.source").value("MOCK"));
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
}
