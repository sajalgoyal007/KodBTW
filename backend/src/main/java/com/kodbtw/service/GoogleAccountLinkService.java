package com.kodbtw.service;

import com.kodbtw.dto.GoogleAuthChallengeResponse;
import com.kodbtw.dto.GoogleIdentityStatusResponse;
import com.kodbtw.dto.GoogleLoginRequest;
import com.kodbtw.entity.User;
import com.kodbtw.exception.GoogleLinkReauthenticationException;
import com.kodbtw.exception.InvalidGoogleCredentialException;
import org.springframework.stereotype.Service;

@Service
public class GoogleAccountLinkService {
    private final AuthService authService;
    private final GoogleIdentityTokenVerifier verifier;
    private final GoogleAuthChallengeService challenges;
    private final GoogleAccountService accounts;

    public GoogleAccountLinkService(AuthService authService, GoogleIdentityTokenVerifier verifier,
                                    GoogleAuthChallengeService challenges, GoogleAccountService accounts) {
        this.authService = authService;
        this.verifier = verifier;
        this.challenges = challenges;
        this.accounts = accounts;
    }

    public GoogleAuthChallengeResponse issueChallenge(User currentUser, String currentPassword) {
        if (currentUser == null || !authService.verifyCurrentPassword(currentUser.getId(), currentPassword)) {
            throw new GoogleLinkReauthenticationException();
        }
        return challenges.issueLink(currentUser.getId());
    }

    public GoogleIdentityStatusResponse link(User currentUser, GoogleLoginRequest request) {
        if (currentUser == null || currentUser.getId() == null) throw new InvalidGoogleCredentialException();

        // Verify all signed Google claims before consuming the user- and operation-bound challenge.
        VerifiedGoogleIdentity verified = verifier.verify(request.credential());
        if (!challenges.consumeLink(request.challengeId(), verified.nonce(), currentUser.getId())) {
            throw new InvalidGoogleCredentialException();
        }
        accounts.linkGoogleIdentity(currentUser, verified);
        return new GoogleIdentityStatusResponse(true);
    }

    public GoogleIdentityStatusResponse status(User currentUser) {
        return new GoogleIdentityStatusResponse(
                currentUser != null && accounts.hasGoogleIdentity(currentUser.getId()));
    }
}
