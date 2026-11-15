package com.kodbtw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodbtw.dto.ProfileRequest;
import com.kodbtw.repository.ProfileRepository;
import com.kodbtw.repository.UserRepository;
import com.kodbtw.support.AuthTestSupport;
import com.kodbtw.support.TestEmailConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestEmailConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private com.kodbtw.repository.PlatformAccountRepository platformAccountRepository;

    @Autowired
    private com.kodbtw.repository.LeaderboardUserCacheRepository leaderboardUserCacheRepository;

    @Autowired
    private com.kodbtw.repository.PlatformStatSnapshotRepository platformStatSnapshotRepository;

    @Autowired
    private com.kodbtw.repository.EmailVerificationTokenRepository emailVerificationTokenRepository;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        platformStatSnapshotRepository.deleteAll();
        leaderboardUserCacheRepository.deleteAll();
        platformAccountRepository.deleteAll();
        profileRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Register, verify, and log in to get a token
        mockMvc.perform(put("/api/profile").contentType(MediaType.APPLICATION_JSON)); // warmup

        userToken = AuthTestSupport.registerAndLogin(
                mockMvc, objectMapper, userRepository, "Sajal Goyal", "sajal@example.com", "password123").token();
    }

    @Test
    void testUnauthenticatedProfileGetReturns401() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUnauthenticatedProfilePutReturns401() throws Exception {
        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetProfileWhenNotCreatedReturns404() throws Exception {
        mockMvc.perform(get("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateAndRetrieveProfile() throws Exception {
        ProfileRequest request = new ProfileRequest();
        request.setDisplayName("Sajal Goyal");
        request.setBio("CS student interested in AI");
        request.setCollege("Arya College of Engineering and IT");
        request.setGraduationYear(2028);
        request.setLocation("Jaipur, Rajasthan");
        request.setGithubUrl("https://github.com/sajalgoyal007");

        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Sajal Goyal"))
                .andExpect(jsonPath("$.indexable").value(false))
                .andExpect(jsonPath("$.college").value("Arya College of Engineering and IT"))
                .andExpect(jsonPath("$.graduationYear").value(2028));

        mockMvc.perform(get("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Sajal Goyal"))
                .andExpect(jsonPath("$.location").value("Jaipur, Rajasthan"))
                .andExpect(jsonPath("$.githubUrl").value("https://github.com/sajalgoyal007"));
    }

    @Test
    void searchEngineIndexingIsOwnerControlledAndDefaultsToOptOut() throws Exception {
        ProfileRequest create = new ProfileRequest();
        create.setUsername("indexoptin");

        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexable").value(false));

        ProfileRequest enable = new ProfileRequest();
        enable.setIndexable(true);
        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enable)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexable").value(true));

        mockMvc.perform(get("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexable").value(true));
    }

    @Test
    void testUpdateProfile() throws Exception {
        ProfileRequest create = new ProfileRequest();
        create.setDisplayName("Initial Name");
        create.setBio("Initial bio");

        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isOk());

        ProfileRequest update = new ProfileRequest();
        update.setDisplayName("Updated Name");
        update.setBio("Updated bio");

        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Updated Name"))
                .andExpect(jsonPath("$.bio").value("Updated bio"));
    }

    @Test
    void testProfileValidationFailureBioTooLong() throws Exception {
        ProfileRequest request = new ProfileRequest();
        request.setBio("x".repeat(1001));

        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void testProfileValidationFailureInvalidGraduationYear() throws Exception {
        ProfileRequest request = new ProfileRequest();
        request.setGraduationYear(1990);

        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}
