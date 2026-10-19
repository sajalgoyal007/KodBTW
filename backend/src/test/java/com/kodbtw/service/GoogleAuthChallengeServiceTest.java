package com.kodbtw.service;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class GoogleAuthChallengeServiceTest {
    private final Instant now = Instant.parse("2026-01-01T00:00:00Z");
    private JdbcTemplate jdbc;
    private GoogleAuthChallengeService service;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:google_challenge_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE google_auth_challenges ("
                + "challenge_id VARCHAR(36) NOT NULL PRIMARY KEY, nonce_hash BINARY(32) NOT NULL, "
                + "operation VARCHAR(16) NOT NULL, user_id BIGINT NULL, expires_at TIMESTAMP(6) NOT NULL, "
                + "created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6))");
        service = serviceAt(now);
    }

    @Test
    void issuedChallengeCanBeConsumedOnlyOnce() {
        var issued = service.issue();

        assertEquals(now.plus(GoogleAuthChallengeService.CHALLENGE_TTL), issued.expiresAt());
        assertTrue(service.consume(issued.challengeId(), issued.nonce()));
        assertFalse(service.consume(issued.challengeId(), issued.nonce()));
    }

    @Test
    void expiredAndUnknownChallengesAreRejected() {
        var issued = service.issue();
        GoogleAuthChallengeService afterExpiry = serviceAt(now.plus(GoogleAuthChallengeService.CHALLENGE_TTL).plusSeconds(1));

        assertFalse(afterExpiry.consume(issued.challengeId(), issued.nonce()));
        assertFalse(service.consume(UUID.randomUUID().toString(), issued.nonce()));
    }

    @Test
    void mismatchedNonceDoesNotConsumeChallengeButMatchingNonceDoes() {
        var issued = service.issue();

        assertFalse(service.consume(issued.challengeId(), "another-nonce"));
        assertTrue(service.consume(issued.challengeId(), issued.nonce()));
    }

    @Test
    void concurrentAttemptsCanConsumeOnlyOnce() throws Exception {
        var issued = service.issue();
        int attempts = 12;
        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        CountDownLatch ready = new CountDownLatch(attempts);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        try {
            for (int index = 0; index < attempts; index++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return service.consume(issued.challengeId(), issued.nonce());
                }));
            }
            ready.await();
            start.countDown();

            long successfulConsumers = 0;
            for (Future<Boolean> result : results) if (result.get()) successfulConsumers++;
            assertEquals(1, successfulConsumers);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void loginChallengeCannotBeConsumedForLinkingAndLinkChallengeIsBoundToItsUser() {
        var login = service.issue();
        var link = service.issueLink(51L);

        assertFalse(service.consumeLink(login.challengeId(), login.nonce(), 51L));
        assertFalse(service.consume(link.challengeId(), link.nonce()));
        assertFalse(service.consumeLink(link.challengeId(), link.nonce(), 52L));
        assertTrue(service.consumeLink(link.challengeId(), link.nonce(), 51L));
        assertFalse(service.consumeLink(link.challengeId(), link.nonce(), 51L));
    }

    @Test
    void linkChallengeExpiresAndCannotBeConsumedByUnknownUser() {
        var issued = service.issueLink(73L);
        GoogleAuthChallengeService afterExpiry = serviceAt(now.plus(GoogleAuthChallengeService.CHALLENGE_TTL).plusSeconds(1));

        assertFalse(afterExpiry.consumeLink(issued.challengeId(), issued.nonce(), 73L));
        assertFalse(service.consumeLink(UUID.randomUUID().toString(), issued.nonce(), 73L));
        assertFalse(service.consumeLink(issued.challengeId(), issued.nonce(), null));
    }

    private GoogleAuthChallengeService serviceAt(Instant instant) {
        return new GoogleAuthChallengeService(jdbc, Clock.fixed(instant, ZoneOffset.UTC), new SecureRandom());
    }
}
