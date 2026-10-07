package com.kodbtw.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodbtw.adapter.codeforces.CodeforcesClient;
import com.kodbtw.adapter.leetcode.LeetCodeClient;
import com.kodbtw.dto.LoginRequest;
import com.kodbtw.dto.PlatformAccountRequest;
import com.kodbtw.dto.ProfileRequest;
import com.kodbtw.dto.RegisterRequest;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.User;
import com.kodbtw.repository.LeaderboardUserCacheRepository;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicProfileControllerTest {

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
    private LeaderboardUserCacheRepository cacheRepository;

    @Autowired
    private PlatformStatSnapshotRepository snapshotRepository;

    @MockBean
    private LeetCodeClient leetCodeClient;

    @MockBean
    private CodeforcesClient codeforcesClient;

    private String userToken;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        snapshotRepository.deleteAll();
        cacheRepository.deleteAll();
        platformAccountRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();

        // Register and login test user
        RegisterRequest register = new RegisterRequest("Test Coder", "coder@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("coder@example.com", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode authNode = objectMapper.readTree(result.getResponse().getContentAsString());
        userToken = authNode.get("token").asText();
        testUser = userRepository.findByEmail("coder@example.com").orElseThrow();
    }

    @Test
    void testPublicProfile_UnauthenticatedSuccess() throws Exception {
        // Create profile
        ProfileRequest profileReq = new ProfileRequest();
        profileReq.setUsername("testcoder");
        profileReq.setDisplayName("Test Coder");
        profileReq.setBio("Passionate developer");
        profileReq.setCollege("Tech Institute");
        profileReq.setGithubUrl("https://github.com/testcoder");

        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isOk());

        // Connect a platform (CodeChef MOCK)
        PlatformAccountRequest paReq = new PlatformAccountRequest();
        paReq.setPlatform(Platform.CODECHEF);
        paReq.setUsername("chef_coder");
        mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paReq)))
                .andExpect(status().isCreated());

        Long accountId = platformAccountRepository.findAllByUserId(testUser.getId()).get(0).getId();
        mockMvc.perform(post("/api/platform-accounts/" + accountId + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk());

        // Access public profile without any authorization header
        mockMvc.perform(get("/api/public/profiles/testcoder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testcoder")))
                .andExpect(jsonPath("$.displayName", is("Test Coder")))
                .andExpect(jsonPath("$.bio", is("Passionate developer")))
                .andExpect(jsonPath("$.college", is("Tech Institute")))
                .andExpect(jsonPath("$.socialLinks.githubUrl", is("https://github.com/testcoder")))
                .andExpect(jsonPath("$.platforms", hasSize(1)))
                .andExpect(jsonPath("$.platforms[0].platform", is("CODECHEF")))
                .andExpect(jsonPath("$.platforms[0].source", is("MOCK")));
    }

    @Test
    void testPublicProfile_SensitiveFieldSanitization() throws Exception {
        ProfileRequest profileReq = new ProfileRequest();
        profileReq.setUsername("securecoder");
        profileReq.setDisplayName("Secure Coder");

        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/public/profiles/securecoder"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(content);

        // Explicitly verify sensitive fields do not exist
        assertFalse(root.has("email"), "Public profile must never expose email");
        assertFalse(root.has("password"), "Public profile must never expose password");
        assertFalse(root.has("userId"), "Public profile must never expose internal userId");
        assertFalse(root.has("id"), "Public profile must never expose internal profile id");
        assertFalse(root.has("token"), "Public profile must never expose token");
        assertFalse(content.contains("coder@example.com"), "Public profile content must not contain email string");
    }

    @Test
    void testPublicProfile_SourceProvenancePreserved() throws Exception {
        ProfileRequest profileReq = new ProfileRequest();
        profileReq.setUsername("sourcetest");
        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isOk());

        // Connect CodeChef (MOCK)
        PlatformAccountRequest mockAccount = new PlatformAccountRequest();
        mockAccount.setPlatform(Platform.CODECHEF);
        mockAccount.setUsername("chef_user");
        mockMvc.perform(post("/api/platform-accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockAccount)))
                .andExpect(status().isCreated());

        Long accountId = platformAccountRepository.findAllByUserId(testUser.getId()).get(0).getId();
        mockMvc.perform(post("/api/platform-accounts/" + accountId + "/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/public/profiles/sourcetest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platforms[0].source", is("MOCK")));
    }

    @Test
    void testPublicProfile_NonexistentUsername_Returns404() throws Exception {
        mockMvc.perform(get("/api/public/profiles/nonexistent_coder"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    void testPublicProfile_InvalidUsernameCharacters_Returns400() throws Exception {
        // Less than 3 chars
        mockMvc.perform(get("/api/public/profiles/ab"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")));

        // Special forbidden chars
        mockMvc.perform(get("/api/public/profiles/user@hack!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    void testPublicProfile_ZeroConnectedPlatforms_Returns200WithEmptyPlatforms() throws Exception {
        ProfileRequest profileReq = new ProfileRequest();
        profileReq.setUsername("zeroplatforms");
        profileReq.setDisplayName("Zero Platforms Coder");

        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/public/profiles/zeroplatforms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("zeroplatforms")))
                .andExpect(jsonPath("$.overview.totalProblemsSolved", is(0)))
                .andExpect(jsonPath("$.platforms", hasSize(0)))
                .andExpect(jsonPath("$.analytics.difficulty.totalProblemsSolved", is(0)));
    }

    @Test
    void testProfileUpdate_UsernameUniquenessEnforced_Returns409() throws Exception {
        // First user sets username "uniquehandle"
        ProfileRequest firstUserReq = new ProfileRequest();
        firstUserReq.setUsername("uniquehandle");
        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUserReq)))
                .andExpect(status().isOk());

        // Register second user
        RegisterRequest register2 = new RegisterRequest("User Two", "user2@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register2)))
                .andExpect(status().isCreated());

        LoginRequest login2 = new LoginRequest("user2@example.com", "password123");
        MvcResult res2 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login2)))
                .andExpect(status().isOk())
                .andReturn();
        String token2 = objectMapper.readTree(res2.getResponse().getContentAsString()).get("token").asText();

        // Second user tries to take "uniquehandle" -> Expect 409 Conflict
        ProfileRequest secondUserReq = new ProfileRequest();
        secondUserReq.setUsername("uniquehandle");
        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUserReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")));
    }

    @Test
    void testProfileUpdate_UsernameNormalizedToLowercase() throws Exception {
        ProfileRequest req = new ProfileRequest();
        req.setUsername("MyCool_Dev-99");
        mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("mycool_dev-99")));

        // Can be fetched in lowercase
        mockMvc.perform(get("/api/public/profiles/mycool_dev-99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("mycool_dev-99")));
    }

    @Test
    void testProfileCreation_FallbackUsernameGeneratedWhenOmitted() throws Exception {
        ProfileRequest req = new ProfileRequest();
        req.setDisplayName("Alice Wonder");

        MvcResult result = mockMvc.perform(put("/api/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        String generated = node.get("username").asText();
        assertNotNull(generated);
        assertTrue(generated.contains("alice-wonder") || generated.startsWith("user-"));

        // Public profile resolves with generated handle
        mockMvc.perform(get("/api/public/profiles/" + generated))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(generated)));
    }
}
