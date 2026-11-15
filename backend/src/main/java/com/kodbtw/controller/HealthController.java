package com.kodbtw.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight liveness endpoint used by Render and uptime monitors.
 *
 * <p>It deliberately does not touch the database so a cold-starting instance can
 * answer health probes before Hikari or Hibernate finish initializing. Spring MVC
 * automatically answers {@code HEAD} requests for these {@code GET} mappings.</p>
 */
@RestController
public class HealthController {

    @GetMapping({"/api/health", "/"})
    public ResponseEntity<Map<String, String>> checkHealth() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Map.of(
                        "status", "UP",
                        "service", "KodBTW Backend"
                ));
    }
}
