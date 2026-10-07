package com.kodbtw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodbtw.dto.LoginRequest;
import com.kodbtw.dto.RegisterRequest;
import com.kodbtw.entity.LeaderboardUserCache;
import com.kodbtw.entity.User;
import com.kodbtw.repository.LeaderboardUserCacheRepository;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaderboardUserCacheRepository cacheRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private String userToken;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        cacheRepository.deleteAll();

        String email = "leaderboard_test_" + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerRequest = new RegisterRequest("Leaderboard User", email, "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(email, "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        userToken = objectMapper.readTree(responseBody).get("token").asText();
        testUser = userRepository.findByEmail(email).orElseThrow();

        // Seed a sample cache row for testUser directly into the database
        jdbcTemplate.update("""
                INSERT INTO leaderboard_user_cache
                    (user_id, display_name, college, total_solved, weighted_score,
                     best_rating, best_rating_platform, total_contests, real_data_only,
                     has_mock_data, last_synced_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """,
                testUser.getId(),
                "Leaderboard User",
                "IIT Delhi",
                250,
                700.0,
                1720,
                "CODEFORCES",
                10,
                true,
                false
        );
    }

    @Test
    void testUnauthenticatedLeaderboardReturns401() throws Exception {
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    void testAuthenticatedGlobalLeaderboardReturns200() throws Exception {
        mockMvc.perform(get("/api/leaderboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "score")
                        .param("dataFilter", "real"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.entries", notNullValue()))
                .andExpect(jsonPath("$.entries[0].displayName", is("Leaderboard User")))
                .andExpect(jsonPath("$.entries[0].realDataOnly", is(true)))
                .andExpect(jsonPath("$.entries[0].isCurrentUser", is(true)));
    }

    @Test
    void testCollegeLeaderboardReturns200() throws Exception {
        mockMvc.perform(get("/api/leaderboard/college")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .param("college", "IIT")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries", notNullValue()))
                .andExpect(jsonPath("$.entries[0].college", is("IIT Delhi")));
    }

    @Test
    void testCollegeLeaderboardBlankReturns400() throws Exception {
        mockMvc.perform(get("/api/leaderboard/college")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .param("college", "   "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetMyRankReturns200() throws Exception {
        mockMvc.perform(get("/api/leaderboard/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.displayName", is("Leaderboard User")))
                .andExpect(jsonPath("$.globalRank", is(1)))
                .andExpect(jsonPath("$.collegeRank", is(1)))
                .andExpect(jsonPath("$.realDataOnly", is(true)));
    }

    @Test
    void testManualSync_UnauthenticatedReturns401() throws Exception {
        mockMvc.perform(post("/api/leaderboard/sync"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testManualSync_OrdinaryUserReturns403Forbidden() throws Exception {
        mockMvc.perform(post("/api/leaderboard/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    void testManualSync_WithAdminAuthorityReturns200() throws Exception {
        UsernamePasswordAuthenticationToken adminAuth = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                List.of(new SimpleGrantedAuthority("ADMIN"))
        );

        mockMvc.perform(post("/api/leaderboard/sync")
                        .with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Leaderboard sync triggered successfully")));
    }
}
