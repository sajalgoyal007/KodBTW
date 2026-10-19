package com.kodbtw.dto;

import java.time.Instant;

public record GoogleAuthChallengeResponse(String challengeId, String nonce, Instant expiresAt) {}
