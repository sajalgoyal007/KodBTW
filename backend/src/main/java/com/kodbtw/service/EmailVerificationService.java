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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final SecureTokenService tokenService;
    private final EmailService emailService;
    private final Duration tokenValidity;
    private final Duration cooldownDuration;
    private final String publicSiteUrl;

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            SecureTokenService tokenService,
            EmailService emailService,
            @Value("${app.verification.token-validity:PT24H}") String tokenValidity,
            @Value("${app.verification.cooldown:PT1M}") String cooldownDuration,
            @Value("${app.public-site-url:https://kodbtw.vercel.app}") String publicSiteUrl) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.tokenValidity = Duration.parse(tokenValidity);
        this.cooldownDuration = Duration.parse(cooldownDuration);
        this.publicSiteUrl = publicSiteUrl == null || publicSiteUrl.isBlank()
                ? "https://kodbtw.vercel.app"
                : publicSiteUrl.replaceAll("/+$", "");
    }

    /**
     * Issues and persists a new verification token, then attempts email delivery.
     *
     * @return true if the email was successfully dispatched; false if email delivery failed.
     */
    @Transactional
    public boolean issueAndSendVerification(User user) {
        String rawToken = tokenService.generateRawToken();
        String tokenHash = tokenService.hashToken(rawToken);

        LocalDateTime expiresAt = LocalDateTime.now().plus(tokenValidity);
        EmailVerificationToken token = new EmailVerificationToken(user, tokenHash, expiresAt);
        tokenRepository.save(token);

        String verificationUrl = publicSiteUrl + "/verify-email?token=" + rawToken;

        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationUrl);
            return true;
        } catch (EmailDeliveryException ex) {
            log.error("Failed to deliver initial verification email for user id {}: {}", user.getId(), ex.getMessage());
            return false;
        }
    }

    /**
     * Atomically consumes a verification token and updates the user's email verification status.
     */
    @Transactional
    public VerifyEmailResponse verifyEmail(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidTokenException("Verification token is required.");
        }

        String tokenHash = tokenService.hashToken(rawToken.trim());
        EmailVerificationToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token."));

        if (token.getUsedAt() != null) {
            throw new TokenAlreadyUsedException("This verification token has already been used.");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("This verification token has expired. Please request a new one.");
        }

        // Atomic consumption and verification
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        User user = token.getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User id {} successfully verified email", user.getId());
        return new VerifyEmailResponse(true, "Your email address has been successfully verified. You can now sign in.");
    }

    /**
     * Handles resending verification emails with cooldown rate-limiting and enumeration protection.
     */
    @Transactional
    public ResendVerificationResponse resendVerification(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email address is required.");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);

        // Anti-enumeration protection: return generic success if user does not exist
        if (userOptional.isEmpty()) {
            log.info("Resend verification requested for non-existent email domain");
            return new ResendVerificationResponse(true,
                    "If an account exists with this email, a verification link has been sent.");
        }

        User user = userOptional.get();

        if (user.isEmailVerified()) {
            return new ResendVerificationResponse(true,
                    "This account is already verified. You can sign in directly.");
        }

        // Abuse protection: check resend cooldown against the most recent token
        Optional<EmailVerificationToken> latestToken = tokenRepository.findTopByUserOrderByCreatedAtDesc(user);
        if (latestToken.isPresent()) {
            Duration elapsed = Duration.between(latestToken.get().getCreatedAt(), LocalDateTime.now());
            if (elapsed.compareTo(cooldownDuration) < 0) {
                long secondsRemaining = Math.max(1, cooldownDuration.toSeconds() - elapsed.toSeconds());
                throw new ResendCooldownException(
                        "Please wait " + secondsRemaining + " seconds before requesting another verification email.",
                        secondsRemaining);
            }
        }

        boolean sent = issueAndSendVerification(user);
        if (!sent) {
            throw new EmailDeliveryException("We could not deliver the verification email right now. Please try again shortly.");
        }

        return new ResendVerificationResponse(true,
                "A verification email has been sent. Please check your inbox.");
    }
}
