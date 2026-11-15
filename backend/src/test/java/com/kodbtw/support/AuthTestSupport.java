package com.kodbtw.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodbtw.dto.LoginRequest;
import com.kodbtw.dto.RegisterRequest;
import com.kodbtw.entity.User;
import com.kodbtw.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared setup helpers for integration tests that need an authenticated session.
 * Registration alone no longer yields a JWT because login is gated on email verification,
 * so tests explicitly mark the freshly-registered user as verified before logging in.
 */
public final class AuthTestSupport {

    private AuthTestSupport() {
    }

    public record AuthSession(String token, User user) {
    }

    public static User register(MockMvc mockMvc, ObjectMapper objectMapper, UserRepository userRepository,
                                String name, String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest(name, email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        return userRepository.findByEmail(normalize(email))
                .orElseThrow(() -> new AssertionError("Registered user should have been persisted: " + email));
    }

    public static User markVerified(UserRepository userRepository, String email) {
        User user = userRepository.findByEmail(normalize(email))
                .orElseThrow(() -> new AssertionError("User should exist: " + email));
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public static String login(MockMvc mockMvc, ObjectMapper objectMapper,
                               String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    public static AuthSession registerAndLogin(MockMvc mockMvc, ObjectMapper objectMapper,
                                               UserRepository userRepository,
                                               String name, String email, String password) throws Exception {
        register(mockMvc, objectMapper, userRepository, name, email, password);
        markVerified(userRepository, email);
        String token = login(mockMvc, objectMapper, email, password);
        User user = userRepository.findByEmail(normalize(email))
                .orElseThrow(() -> new AssertionError("Registered user should have been persisted: " + email));
        return new AuthSession(token, user);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
