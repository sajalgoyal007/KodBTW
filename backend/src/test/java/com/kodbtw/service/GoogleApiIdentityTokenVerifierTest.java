package com.kodbtw.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.kodbtw.exception.GoogleAuthConfigurationException;
import com.kodbtw.exception.InvalidGoogleCredentialException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleApiIdentityTokenVerifierTest {
    @Test
    void missingClientIdFailsOnlyGoogleVerification() {
        GoogleApiIdentityTokenVerifier verifier = new GoogleApiIdentityTokenVerifier("", mock(GoogleIdTokenVerifier.class));
        assertThrows(GoogleAuthConfigurationException.class, () -> verifier.verify("credential"));
    }

    @Test
    void googleLibraryRejectionsForInvalidExpiredAndWrongAudienceTokensAreRejected() throws Exception {
        GoogleIdTokenVerifier googleVerifier = mock(GoogleIdTokenVerifier.class);
        for (String token : new String[]{"malformed-token", "expired-token", "wrong-audience-token"}) {
            when(googleVerifier.verify(token)).thenReturn(null);
            GoogleApiIdentityTokenVerifier verifier = new GoogleApiIdentityTokenVerifier("expected-client-id", googleVerifier);
            assertThrows(InvalidGoogleCredentialException.class, () -> verifier.verify(token));
        }
    }

    @Test
    void verifiedTokenMustContainVerifiedEmailAndMatchingNonce() throws Exception {
        GoogleIdTokenVerifier googleVerifier = mock(GoogleIdTokenVerifier.class);
        GoogleIdToken token = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(googleVerifier.verify("valid-token")).thenReturn(token);
        when(token.getPayload()).thenReturn(payload);
        when(payload.getSubject()).thenReturn("stable-google-sub");
        when(payload.getEmail()).thenReturn("person@example.com");
        when(payload.getEmailVerified()).thenReturn(true);
        when(payload.get("nonce")).thenReturn("expected-nonce");
        when(payload.get("name")).thenReturn("Person");

        GoogleApiIdentityTokenVerifier verifier = new GoogleApiIdentityTokenVerifier("expected-client-id", googleVerifier);
        VerifiedGoogleIdentity identity = verifier.verify("valid-token");
        assertEquals("stable-google-sub", identity.subject());
        assertEquals("person@example.com", identity.email());
        assertEquals("Person", identity.name());

        assertEquals("expected-nonce", identity.nonce());
        when(payload.get("nonce")).thenReturn(null);
        assertThrows(InvalidGoogleCredentialException.class, () -> verifier.verify("valid-token"));
        when(payload.get("nonce")).thenReturn("expected-nonce");
        when(payload.getEmailVerified()).thenReturn(false);
        assertThrows(InvalidGoogleCredentialException.class, () -> verifier.verify("valid-token"));
    }
}
