package com.kodbtw.service;

public record VerifiedGoogleIdentity(String subject, String email, String name, String nonce) {}
