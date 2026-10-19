package com.kodbtw.service;

import com.kodbtw.dto.GoogleLoginRequest;
import com.kodbtw.dto.LoginResponse;
import com.kodbtw.dto.UserResponse;
import com.kodbtw.entity.User;
import com.kodbtw.exception.GoogleIdentityConflictException;
import com.kodbtw.exception.InvalidGoogleCredentialException;
import com.kodbtw.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class GoogleAuthService {
    private final GoogleIdentityTokenVerifier verifier;
    private final GoogleAuthChallengeService challenges;
    private final GoogleAccountService accounts;
    private final JwtService jwtService;

    public GoogleAuthService(GoogleIdentityTokenVerifier verifier, GoogleAuthChallengeService challenges,
                             GoogleAccountService accounts, JwtService jwtService) {
        this.verifier = verifier;
        this.challenges = challenges;
        this.accounts = accounts;
        this.jwtService = jwtService;
    }

    public LoginResponse authenticate(GoogleLoginRequest request) {
        // Verify the signed token and its standard claims before touching challenge state.
        VerifiedGoogleIdentity verified = verifier.verify(request.credential());
        if (!challenges.consume(request.challengeId(), verified.nonce())) {
            throw new InvalidGoogleCredentialException();
        }
        User user;
        try {
            user = accounts.resolveOrCreate(verified);
        } catch (DataIntegrityViolationException race) {
            // Unique DB constraints serialize duplicate identities and email races. A concurrent
            // request for the same Google subject can safely reuse the account created by its peer.
            user = accounts.findLinkedUser(verified.subject()).orElseThrow(GoogleIdentityConflictException::new);
        }
        return new LoginResponse(jwtService.generateToken(user), new UserResponse(user.getId(), user.getName(), user.getEmail()));
    }
}
