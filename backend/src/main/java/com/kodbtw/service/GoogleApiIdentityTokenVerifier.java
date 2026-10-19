package com.kodbtw.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.kodbtw.exception.GoogleAuthConfigurationException;
import com.kodbtw.exception.GoogleIdentityVerificationException;
import com.kodbtw.exception.InvalidGoogleCredentialException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
public class GoogleApiIdentityTokenVerifier implements GoogleIdentityTokenVerifier {
    private final String clientId;
    private volatile GoogleIdTokenVerifier verifier;

    @Autowired
    public GoogleApiIdentityTokenVerifier(@Value("${app.google-auth.client-id:}") String clientId) {
        this.clientId = clientId == null ? "" : clientId.trim();
    }

    GoogleApiIdentityTokenVerifier(String clientId, GoogleIdTokenVerifier verifier) {
        this.clientId = clientId == null ? "" : clientId.trim();
        this.verifier = verifier;
    }

    @Override
    public VerifiedGoogleIdentity verify(String credential) {
        if (clientId.isBlank()) throw new GoogleAuthConfigurationException();
        try {
            GoogleIdToken token = getVerifier().verify(credential);
            if (token == null) throw new InvalidGoogleCredentialException();

            GoogleIdToken.Payload payload = token.getPayload();
            String subject = payload.getSubject();
            String email = payload.getEmail();
            String tokenNonce = payload.get("nonce") instanceof String value ? value : null;
            Object emailVerified = payload.getEmailVerified();
            if (subject == null || subject.isBlank() || email == null || email.isBlank()
                    || !Boolean.TRUE.equals(emailVerified)
                    || tokenNonce == null || tokenNonce.isBlank()) {
                throw new InvalidGoogleCredentialException();
            }
            Object rawName = payload.get("name");
            String name = rawName instanceof String value && !value.isBlank() ? value.trim() : email;
            return new VerifiedGoogleIdentity(subject, email, name, tokenNonce);
        } catch (InvalidGoogleCredentialException | GoogleAuthConfigurationException exception) {
            throw exception;
        } catch (IOException | GeneralSecurityException exception) {
            throw new GoogleIdentityVerificationException();
        }
    }

    private GoogleIdTokenVerifier getVerifier() throws IOException, GeneralSecurityException {
        GoogleIdTokenVerifier current = verifier;
        if (current == null) {
            synchronized (this) {
                current = verifier;
                if (current == null) {
                    current = new GoogleIdTokenVerifier.Builder(
                            GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance())
                            .setAudience(List.of(clientId))
                            .build();
                    verifier = current;
                }
            }
        }
        return current;
    }
}
