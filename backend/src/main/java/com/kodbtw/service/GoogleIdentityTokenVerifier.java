package com.kodbtw.service;

public interface GoogleIdentityTokenVerifier {
    VerifiedGoogleIdentity verify(String credential);
}
