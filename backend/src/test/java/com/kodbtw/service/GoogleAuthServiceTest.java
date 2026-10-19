package com.kodbtw.service;

import com.kodbtw.dto.GoogleLoginRequest;
import com.kodbtw.dto.LoginResponse;
import com.kodbtw.entity.User;
import com.kodbtw.exception.GoogleAccountExistsException;
import com.kodbtw.exception.GoogleIdentityConflictException;
import com.kodbtw.exception.InvalidGoogleCredentialException;
import com.kodbtw.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleAuthServiceTest {
    private GoogleIdentityTokenVerifier verifier;
    private GoogleAuthChallengeService challenges;
    private GoogleAccountService accounts;
    private JwtService jwtService;
    private GoogleAuthService service;
    private final VerifiedGoogleIdentity identity = new VerifiedGoogleIdentity(
            "google-sub", "person@example.com", "Person", "expected-nonce");

    @BeforeEach
    void setUp() {
        verifier = mock(GoogleIdentityTokenVerifier.class);
        challenges = mock(GoogleAuthChallengeService.class);
        accounts = mock(GoogleAccountService.class);
        jwtService = mock(JwtService.class);
        service = new GoogleAuthService(verifier, challenges, accounts, jwtService);
    }

    @Test
    void verifiedIdentityIssuesExistingKodBtwJwt() {
        User user = new User("Person", "person@example.com", null);
        user.setId(11L);
        when(verifier.verify("id-token")).thenReturn(identity);
        when(challenges.consume("challenge-id", "expected-nonce")).thenReturn(true);
        when(accounts.resolveOrCreate(identity)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("kodbtw-jwt");

        LoginResponse response = service.authenticate(new GoogleLoginRequest("id-token", "challenge-id"));

        assertEquals("kodbtw-jwt", response.getToken());
        assertEquals(11L, response.getUser().getId());
        verify(jwtService).generateToken(user);
    }

    @Test
    void invalidExpiredAndWrongAudienceTokensAreRejectedBeforeAccountLookup() {
        // The production verifier delegates signature, expiry, issuer, and audience checks to Google's
        // GoogleIdTokenVerifier; each rejected-token outcome maps to invalid credentials.
        for (String rejectedToken : new String[]{"invalid", "expired", "wrong-audience"}) {
            when(verifier.verify(rejectedToken)).thenThrow(new InvalidGoogleCredentialException());
            assertThrows(InvalidGoogleCredentialException.class, () -> service.authenticate(
                    new GoogleLoginRequest(rejectedToken, "challenge-id")));
        }
        verifyNoInteractions(challenges, accounts, jwtService);
    }

    @Test
    void invalidChallengeIsRejectedAfterTokenVerificationAndBeforeAccountLookup() {
        when(verifier.verify("id-token")).thenReturn(identity);
        when(challenges.consume("challenge-id", "expected-nonce")).thenReturn(false);

        assertThrows(InvalidGoogleCredentialException.class, () -> service.authenticate(
                new GoogleLoginRequest("id-token", "challenge-id")));
        verify(verifier).verify("id-token");
        verify(challenges).consume("challenge-id", "expected-nonce");
        verifyNoInteractions(accounts, jwtService);
    }

    @Test
    void emailCollisionRequiresExistingSignInAndDoesNotIssueJwt() {
        when(verifier.verify("id-token")).thenReturn(identity);
        when(challenges.consume("challenge-id", "expected-nonce")).thenReturn(true);
        when(accounts.resolveOrCreate(identity)).thenThrow(new GoogleAccountExistsException());

        assertThrows(GoogleAccountExistsException.class, () -> service.authenticate(
                new GoogleLoginRequest("id-token", "challenge-id")));
        verifyNoInteractions(jwtService);
    }

    @Test
    void concurrentDuplicateGoogleRequestReusesTheIdentityCreatedByItsPeer() {
        User user = new User("Person", "person@example.com", null);
        user.setId(12L);
        when(verifier.verify("id-token")).thenReturn(identity);
        when(challenges.consume("challenge-id", "expected-nonce")).thenReturn(true);
        when(accounts.resolveOrCreate(identity)).thenThrow(new DataIntegrityViolationException("unique constraint"));
        when(accounts.findLinkedUser("google-sub")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("kodbtw-jwt");

        assertEquals("kodbtw-jwt", service.authenticate(new GoogleLoginRequest("id-token", "challenge-id")).getToken());
        verify(accounts).findLinkedUser("google-sub");
    }

    @Test
    void concurrentEmailCollisionWithoutLinkedSubjectReturnsSafeConflict() {
        when(verifier.verify("id-token")).thenReturn(identity);
        when(challenges.consume("challenge-id", "expected-nonce")).thenReturn(true);
        when(accounts.resolveOrCreate(identity)).thenThrow(new DataIntegrityViolationException("unique constraint"));
        when(accounts.findLinkedUser("google-sub")).thenReturn(Optional.empty());

        assertThrows(GoogleIdentityConflictException.class, () -> service.authenticate(
                new GoogleLoginRequest("id-token", "challenge-id")));
        verifyNoInteractions(jwtService);
    }
}
