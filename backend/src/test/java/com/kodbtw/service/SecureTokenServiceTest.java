package com.kodbtw.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecureTokenServiceTest {

    private final SecureTokenService tokenService = new SecureTokenService();

    @Test
    void generatedTokensAreUrlSafeAndUnique() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            String token = tokenService.generateRawToken();
            assertThat(token).matches("[A-Za-z0-9_-]{43}");
            tokens.add(token);
        }
        assertThat(tokens).hasSize(50);
    }

    @Test
    void hashingProducesDeterministicSha256Hex() {
        assertThat(tokenService.hashToken("hello"))
                .isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
        assertThat(tokenService.hashToken("hello"))
                .isEqualTo(tokenService.hashToken("  hello  "));
        assertThat(tokenService.hashToken("hello")).hasSize(64);
    }

    @Test
    void hashingRejectsNullAndBlankTokens() {
        assertThatThrownBy(() -> tokenService.hashToken(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> tokenService.hashToken("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
