package com.kodbtw.service;

import com.kodbtw.dto.LoginRequest;
import com.kodbtw.dto.LoginResponse;
import com.kodbtw.dto.UserResponse;
import com.kodbtw.config.AuthRequestDiagnosticsFilter;
import com.kodbtw.entity.User;
import com.kodbtw.exception.InvalidCredentialsException;
import com.kodbtw.repository.UserRepository;
import com.kodbtw.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        Optional<User> userLookup = timedStage("email_lookup", () -> userRepository.findByEmail(normalizedEmail));
        User user = userLookup
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        boolean passwordMatches = timedStage("password_verification",
                () -> passwordEncoder.matches(request.getPassword(), user.getPassword()));
        if (!passwordMatches) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = timedStage("jwt_generation", () -> jwtService.generateToken(user));
        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail());

        return new LoginResponse(token, userResponse);
    }

    private <T> T timedStage(String stage, Supplier<T> operation) {
        String requestId = MDC.get(AuthRequestDiagnosticsFilter.REQUEST_ID_MDC_KEY);
        log.info("auth_stage_started request_id={} stage={}", requestId, stage);
        long startedAt = System.nanoTime();
        try {
            return operation.get();
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("auth_stage_completed request_id={} stage={} duration_ms={}",
                    requestId, stage, durationMs);
        }
    }

    @FunctionalInterface
    private interface Supplier<T> {
        T get();
    }
}
