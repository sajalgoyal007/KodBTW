package com.kodbtw.service;

import com.kodbtw.dto.GoogleAuthChallengeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class GoogleAuthChallengeService {
    static final Duration CHALLENGE_TTL = Duration.ofMinutes(5);
    private static final String LOGIN_OPERATION = "LOGIN";
    private static final String LINK_OPERATION = "LINK";

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;
    private final SecureRandom secureRandom;

    @Autowired
    public GoogleAuthChallengeService(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, Clock.systemUTC(), new SecureRandom());
    }

    GoogleAuthChallengeService(JdbcTemplate jdbcTemplate, Clock clock, SecureRandom secureRandom) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    public GoogleAuthChallengeResponse issue() {
        return issue(LOGIN_OPERATION, null);
    }

    public GoogleAuthChallengeResponse issueLink(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("A user is required for a Google link challenge");
        }
        return issue(LINK_OPERATION, userId);
    }

    private GoogleAuthChallengeResponse issue(String operation, Long userId) {
        Instant now = clock.instant();
        jdbcTemplate.update("DELETE FROM google_auth_challenges WHERE expires_at <= ?", Timestamp.from(now));

        byte[] randomNonce = new byte[32];
        secureRandom.nextBytes(randomNonce);
        String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(randomNonce);
        String challengeId = UUID.randomUUID().toString();
        Instant expiresAt = now.plus(CHALLENGE_TTL);

        jdbcTemplate.update(
                "INSERT INTO google_auth_challenges (challenge_id, nonce_hash, operation, user_id, expires_at) "
                        + "VALUES (?, ?, ?, ?, ?)",
                challengeId, sha256(nonce), operation, userId, Timestamp.from(expiresAt));
        return new GoogleAuthChallengeResponse(challengeId, nonce, expiresAt);
    }

    /** Atomically consumes a challenge only when its id, nonce and expiry all match. */
    public boolean consume(String challengeId, String verifiedNonce) {
        return consume(challengeId, verifiedNonce, LOGIN_OPERATION, null);
    }

    public boolean consumeLink(String challengeId, String verifiedNonce, Long userId) {
        if (userId == null || userId <= 0) return false;
        return consume(challengeId, verifiedNonce, LINK_OPERATION, userId);
    }

    private boolean consume(String challengeId, String verifiedNonce, String operation, Long userId) {
        if (challengeId == null || challengeId.isBlank() || challengeId.length() > 36
                || verifiedNonce == null || verifiedNonce.isBlank()) {
            return false;
        }
        String sql = "DELETE FROM google_auth_challenges WHERE challenge_id = ? AND nonce_hash = ? "
                + "AND expires_at > ? AND operation = ? AND "
                + (userId == null ? "user_id IS NULL" : "user_id = ?");
        int deleted = userId == null
                ? jdbcTemplate.update(sql, challengeId, sha256(verifiedNonce), Timestamp.from(clock.instant()), operation)
                : jdbcTemplate.update(sql, challengeId, sha256(verifiedNonce), Timestamp.from(clock.instant()), operation, userId);
        return deleted == 1;
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
