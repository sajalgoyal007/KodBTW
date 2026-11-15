package com.kodbtw.service;

import com.kodbtw.dto.ResendVerificationResponse;
import com.kodbtw.dto.VerifyEmailResponse;
import com.kodbtw.entity.EmailVerificationToken;
import com.kodbtw.entity.User;
import com.kodbtw.exception.EmailDeliveryException;
import com.kodbtw.exception.InvalidTokenException;
import com.kodbtw.exception.ResendCooldownException;
import com.kodbtw.exception.TokenAlreadyUsedException;
import com.kodbtw.exception.TokenExpiredException;
import com.kodbtw.repository.EmailVerificationTokenRepository;
import com.kodbtw.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecureTokenService tokenService;

    @Mock
    private EmailService emailService;

    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(
                tokenRepository, userRepository, tokenService, emailService,
                "PT24H", "PT1M", "https://app.example/");
    }

    @Test
    void issueAndSendVerificationPersistsHashedTokenAndSendsLink() {
        User user = user("user@example.com");
        when(tokenService.generateRawToken()).thenReturn("raw-token");
        when(tokenService.hashToken("raw-token")).thenReturn("hashed-token");

        boolean sent = service.issueAndSendVerification(user);

        assertThat(sent).isTrue();
        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        EmailVerificationToken saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getTokenHash()).isEqualTo("hashed-token");
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now().plusHours(23));
        assertThat(saved.getUsedAt()).isNull();

        verify(emailService).sendVerificationEmail(
                "user@example.com", "Test User", "https://app.example/verify-email?token=raw-token");
    }

    @Test
    void issueAndSendVerificationReturnsFalseWhenDeliveryFails() {
        User user = user("user@example.com");
        when(tokenService.generateRawToken()).thenReturn("raw-token");
        when(tokenService.hashToken("raw-token")).thenReturn("hashed-token");
        doThrow(new EmailDeliveryException("provider unavailable"))
                .when(emailService).sendVerificationEmail(any(), any(), any());

        boolean sent = service.issueAndSendVerification(user);

        assertThat(sent).isFalse();
        verify(tokenRepository).save(any(EmailVerificationToken.class));
    }

    @Test
    void verifyEmailMarksTokenUsedAndUserVerified() {
        User user = user("user@example.com");
        EmailVerificationToken token = token(user, "hashed-token", LocalDateTime.now().plusHours(1));
        when(tokenService.hashToken("raw-token")).thenReturn("hashed-token");
        when(tokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        VerifyEmailResponse response = service.verifyEmail("  raw-token  ");

        assertThat(response.isSuccess()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getEmailVerifiedAt()).isNotNull();
        verify(tokenRepository).save(token);
        verify(userRepository).save(user);
    }

    @Test
    void verifyEmailRejectsMissingToken() {
        assertThat(catchThrowableOfType(() -> service.verifyEmail(" "), InvalidTokenException.class))
                .isNotNull();
        verifyNoInteractions(tokenRepository, userRepository, emailService);
    }

    @Test
    void verifyEmailRejectsUnknownToken() {
        when(tokenService.hashToken("unknown")).thenReturn("missing-hash");
        when(tokenRepository.findByTokenHash("missing-hash")).thenReturn(Optional.empty());

        assertThat(catchThrowableOfType(() -> service.verifyEmail("unknown"), InvalidTokenException.class))
                .isNotNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmailRejectsExpiredToken() {
        User user = user("user@example.com");
        EmailVerificationToken token = token(user, "hashed-token", LocalDateTime.now().minusSeconds(1));
        when(tokenService.hashToken("raw-token")).thenReturn("hashed-token");
        when(tokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        assertThat(catchThrowableOfType(() -> service.verifyEmail("raw-token"), TokenExpiredException.class))
                .isNotNull();
        assertThat(token.getUsedAt()).isNull();
        assertThat(user.isEmailVerified()).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmailRejectsAlreadyUsedToken() {
        User user = user("user@example.com");
        EmailVerificationToken token = token(user, "hashed-token", LocalDateTime.now().plusHours(1));
        token.setUsedAt(LocalDateTime.now().minusMinutes(5));
        when(tokenService.hashToken("raw-token")).thenReturn("hashed-token");
        when(tokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        assertThat(catchThrowableOfType(() -> service.verifyEmail("raw-token"), TokenAlreadyUsedException.class))
                .isNotNull();
        assertThat(user.isEmailVerified()).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void resendVerificationReturnsGenericSuccessForUnknownEmail() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResendVerificationResponse response = service.resendVerification(" Missing@Example.com ");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("If an account exists");
        verifyNoInteractions(emailService, tokenRepository);
    }

    @Test
    void resendVerificationReturnsSuccessForAlreadyVerifiedAccount() {
        User user = user("verified@example.com");
        user.setEmailVerified(true);
        when(userRepository.findByEmail("verified@example.com")).thenReturn(Optional.of(user));

        ResendVerificationResponse response = service.resendVerification("verified@example.com");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("already verified");
        verifyNoInteractions(emailService);
        verifyNoInteractions(tokenRepository);
    }

    @Test
    void resendVerificationEnforcesCooldown() {
        User user = user("user@example.com");
        EmailVerificationToken recent = token(user, "old-hash", LocalDateTime.now().plusHours(1));
        recent.setCreatedAt(LocalDateTime.now());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(recent));

        ResendCooldownException exception = catchThrowableOfType(
                () -> service.resendVerification("user@example.com"), ResendCooldownException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getRetryAfterSeconds()).isBetween(1L, 60L);
        verifyNoInteractions(emailService);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void resendVerificationSendsNewLinkAfterCooldown() {
        User user = user("user@example.com");
        EmailVerificationToken old = token(user, "old-hash", LocalDateTime.now().plusHours(1));
        old.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(old));
        when(tokenService.generateRawToken()).thenReturn("new-raw");
        when(tokenService.hashToken("new-raw")).thenReturn("new-hash");

        ResendVerificationResponse response = service.resendVerification("USER@example.com");

        assertThat(response.isSuccess()).isTrue();
        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isEqualTo("new-hash");
        verify(emailService).sendVerificationEmail(
                "user@example.com", "Test User", "https://app.example/verify-email?token=new-raw");
    }

    @Test
    void resendVerificationSurfacesDeliveryFailure() {
        User user = user("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());
        when(tokenService.generateRawToken()).thenReturn("new-raw");
        when(tokenService.hashToken("new-raw")).thenReturn("new-hash");
        doThrow(new EmailDeliveryException("provider unavailable"))
                .when(emailService).sendVerificationEmail(any(), any(), any());

        assertThat(catchThrowableOfType(
                () -> service.resendVerification("user@example.com"), EmailDeliveryException.class))
                .isNotNull();
    }

    @Test
    void resendVerificationRejectsBlankEmail() {
        assertThat(catchThrowableOfType(
                () -> service.resendVerification("  "), IllegalArgumentException.class)).isNotNull();
        verifyNoInteractions(userRepository, tokenRepository, emailService);
    }

    private User user(String email) {
        User user = new User("Test User", email, "encoded-password");
        user.setId(7L);
        return user;
    }

    private EmailVerificationToken token(User user, String hash, LocalDateTime expiresAt) {
        return new EmailVerificationToken(user, hash, expiresAt);
    }
}
