package com.kodbtw.adapter.contest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ContestProviderSelectionValidator {
    private static final Set<String> SUPPORTED_PROVIDERS = Set.of("codeforces", "fixture");

    public ContestProviderSelectionValidator(
            @Value("${contest-calendar.provider:codeforces}") String provider) {
        if (provider == null || !SUPPORTED_PROVIDERS.contains(provider)) {
            throw new IllegalStateException(
                    "Invalid contest-calendar.provider. Supported values are 'codeforces' and 'fixture'.");
        }
    }
}
