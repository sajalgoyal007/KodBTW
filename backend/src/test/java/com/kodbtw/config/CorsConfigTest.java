package com.kodbtw.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CorsConfigTest {
    @Test
    void parsesCommaSeparatedExplicitOrigins() {
        assertEquals(List.of("http://localhost:5173", "https://kodbtw.example"),
                CorsConfig.parseAllowedOrigins(" http://localhost:5173, https://kodbtw.example "));
    }

    @Test
    void rejectsWildcardAndEmptyOrigins() {
        assertThrows(IllegalArgumentException.class, () -> CorsConfig.parseAllowedOrigins("*"));
        assertThrows(IllegalArgumentException.class, () -> CorsConfig.parseAllowedOrigins(" ,  "));
    }
}
