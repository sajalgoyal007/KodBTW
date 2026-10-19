package com.kodbtw.service;

import com.kodbtw.dto.GoogleAuthChallengeResponse;
import com.kodbtw.dto.GoogleIdentityStatusResponse;
import com.kodbtw.dto.GoogleLinkChallengeRequest;
import com.kodbtw.dto.GoogleLoginRequest;
import com.kodbtw.entity.User;
import com.kodbtw.exception.GoogleLinkReauthenticationException;
import com.kodbtw.exception.InvalidGoogleCredentialException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleAccountLinkServiceTest {
    private AuthService authService;
    private GoogleIdentityTokenVerifier verifier;
    private GoogleAuthChallengeService challenges;
    private GoogleAccountService accounts;
    private GoogleAccountLinkService service;
    private User user;
    private final VerifiedGoogleIdentity identity = new VerifiedGoogleIdentity(
            "google-sub", "verified@example.com", "Verified", "nonce");

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        verifier = mock(GoogleIdentityTokenVerifier.class);
        challenges = mock(GoogleAuthChallengeService.class);
        accounts = mock(GoogleAccountService.class);
        service = new GoogleAccountLinkService(authService, verifier, challenges, accounts);
        user = new User("Owner", "owner@example.com", "password-hash");
        user.setId(44L);
    }

    @Test
    void passwordIsVerifiedBeforeIssuingUserBoundLinkChallenge() {
        var response = new GoogleAuthChallengeResponse("challenge", "nonce", Instant.parse("2026-01-01T00:05:00Z"));
        when(authService.verifyCurrentPassword(44L, "current-password")).thenReturn(true);
        when(challenges.issueLink(44L)).thenReturn(response);

        assertSame(response, service.issueChallenge(user, "current-password"));
        verify(challenges).issueLink(44L);
    }

    @Test
    void incorrectPasswordDoesNotIssueChallenge() {
        when(authService.verifyCurrentPassword(44L, "wrong-password")).thenReturn(false);

        assertThrows(GoogleLinkReauthenticationException.class,
                () -> service.issueChallenge(user, "wrong-password"));
        verifyNoInteractions(challenges);
    }

    @Test
    void linkUsesVerifiedSubjectAndAuthenticatedUserBoundChallenge() {
        when(verifier.verify("fixture-credential")).thenReturn(identity);
        when(challenges.consumeLink("challenge-id", "nonce", 44L)).thenReturn(true);

        GoogleIdentityStatusResponse result = service.link(user,
                new GoogleLoginRequest("fixture-credential", "challenge-id"));

        assertTrue(result.connected());
        verify(accounts).linkGoogleIdentity(user, identity);
    }

    @Test
    void invalidCredentialDoesNotConsumeChallengeOrLinkIdentity() {
        when(verifier.verify("fixture-credential")).thenReturn(identity);
        when(challenges.consumeLink("challenge-id", "nonce", 44L)).thenReturn(false);

        assertThrows(InvalidGoogleCredentialException.class, () -> service.link(user,
                new GoogleLoginRequest("fixture-credential", "challenge-id")));
        verifyNoInteractions(accounts);
    }

    @Test
    void statusDoesNotExposeGoogleIdentityDetails() {
        when(accounts.hasGoogleIdentity(44L)).thenReturn(true);
        assertTrue(service.status(user).connected());
    }
}
