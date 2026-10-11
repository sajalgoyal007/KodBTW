package com.kodbtw.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.kodbtw.config.AuthRequestDiagnosticsFilter;
import com.kodbtw.dto.LoginRequest;
import com.kodbtw.entity.User;
import com.kodbtw.repository.UserRepository;
import com.kodbtw.security.JwtService;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void loginLogsStageDurationsWithoutCredentialsOrToken() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        User user = new User("Private Name", "private@example.com", "encoded-password");
        user.setId(9L);
        when(userRepository.findByEmail("private@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password-must-not-be-logged", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-must-not-be-logged");

        Logger logger = (Logger) LoggerFactory.getLogger(AuthService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        MDC.put(AuthRequestDiagnosticsFilter.REQUEST_ID_MDC_KEY, "e7814d99-4358-4bc4-998c-066d93d44f50");

        try {
            new AuthService(userRepository, passwordEncoder, jwtService)
                    .login(new LoginRequest("PRIVATE@example.com", "password-must-not-be-logged"));

            String logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage)
                    .reduce("", (left, right) -> left + "\n" + right);
            assertTrue(logs.contains("stage=email_lookup"));
            assertTrue(logs.contains("stage=password_verification"));
            assertTrue(logs.contains("stage=jwt_generation"));
            assertTrue(logs.contains("duration_ms="));
            assertTrue(logs.contains("e7814d99-4358-4bc4-998c-066d93d44f50"));
            assertFalse(logs.contains("private@example.com"));
            assertFalse(logs.contains("Private Name"));
            assertFalse(logs.contains("password-must-not-be-logged"));
            assertFalse(logs.contains("encoded-password"));
            assertFalse(logs.contains("jwt-must-not-be-logged"));
            assertTrue(appender.list.stream().allMatch(event -> event.getThrowableProxy() == null));
        } finally {
            MDC.remove(AuthRequestDiagnosticsFilter.REQUEST_ID_MDC_KEY);
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
