package com.kodbtw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodbtw.dto.LoginRequest;
import com.kodbtw.dto.RegisterRequest;
import com.kodbtw.entity.User;
import com.kodbtw.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.kodbtw.repository.ProfileRepository profileRepository;

    @Autowired
    private com.kodbtw.repository.PlatformAccountRepository platformAccountRepository;

    @Autowired
    private com.kodbtw.repository.LeaderboardUserCacheRepository leaderboardUserCacheRepository;

    @Autowired
    private com.kodbtw.repository.PlatformStatSnapshotRepository platformStatSnapshotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void setUp() {
        platformStatSnapshotRepository.deleteAll();
        leaderboardUserCacheRepository.deleteAll();
        platformAccountRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    // --- Phase 3A: Registration Tests ---

    @Test
    void testSuccessfulRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest("Sajal Goyal", "sajal@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Sajal Goyal"))
                .andExpect(jsonPath("$.email").value("sajal@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testPasswordIsStoredHashedNeverPlaintext() throws Exception {
        String rawPassword = "securePassword123";
        RegisterRequest request = new RegisterRequest("Test User", "testuser@example.com", rawPassword);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        User savedUser = userRepository.findByEmail("testuser@example.com")
                .orElseThrow(() -> new AssertionError("User should have been persisted"));

        assertThat(savedUser.getPassword()).isNotEqualTo(rawPassword);
        assertThat(savedUser.getPassword()).startsWith("$2a$");
        assertThat(passwordEncoder.matches(rawPassword, savedUser.getPassword())).isTrue();
    }

    @Test
    void testDuplicateEmailRegistrationFails() throws Exception {
        RegisterRequest firstRequest = new RegisterRequest("First User", "duplicate@example.com", "password123");
        RegisterRequest secondRequest = new RegisterRequest("Second User", "duplicate@example.com", "newpassword456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    void testValidationFailureMissingName() throws Exception {
        RegisterRequest request = new RegisterRequest("", "valid@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    void testValidationFailureInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("Valid Name", "not-an-email", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void testValidationFailureShortPassword() throws Exception {
        RegisterRequest request = new RegisterRequest("Valid Name", "valid@example.com", "12345");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.password").exists());
    }

    // --- Phase 3B: Login and JWT Tests ---

    @Test
    void testSuccessfulLogin() throws Exception {
        // Register user first
        RegisterRequest registerReq = new RegisterRequest("Sajal Goyal", "sajal@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Perform login
        LoginRequest loginReq = new LoginRequest("sajal@example.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token", startsWith("eyJ")))
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.name").value("Sajal Goyal"))
                .andExpect(jsonPath("$.user.email").value("sajal@example.com"));
    }

    @Test
    void testLoginInvalidPassword() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("Sajal Goyal", "sajal@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("sajal@example.com", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message", containsString("Invalid email or password")));
    }

    @Test
    void testLoginUnknownEmail() throws Exception {
        LoginRequest loginReq = new LoginRequest("unknown@example.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message", containsString("Invalid email or password")));
    }

    @Test
    void testProtectedMeEndpointWithValidJwt() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("Sajal Goyal", "sajal@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("sajal@example.com", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("token").asText();

        // Call protected /api/auth/me with valid JWT
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sajal Goyal"))
                .andExpect(jsonPath("$.email").value("sajal@example.com"));
    }

    @Test
    void testProtectedMeEndpointWithoutJwtReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedMeEndpointWithInvalidJwtReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.signature"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedMeEndpointWithExpiredJwtReturns401() throws Exception {
        // Construct an already-expired JWT token
        Date pastIssued = new Date(System.currentTimeMillis() - 100000);
        Date pastExpiry = new Date(System.currentTimeMillis() - 10000);

        String expiredToken = Jwts.builder()
                .subject("1")
                .claim("email", "test@example.com")
                .issuedAt(pastIssued)
                .expiration(pastExpiry)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}
