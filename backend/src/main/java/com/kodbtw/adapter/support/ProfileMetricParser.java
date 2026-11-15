package com.kodbtw.adapter.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small, defensive helper for extracting a few known numeric fields from public profile HTML.
 *
 * <p>These platforms do not publish a documented JSON statistics API. When a field cannot be
 * found the value stays null; the caller decides whether enough data was found to persist a
 * snapshot. Parsing is intentionally limited to a handful of well-known field names.
 */
public final class ProfileMetricParser {

    private ProfileMetricParser() {}

    /** Finds the first numeric value for any of the supplied field names. */
    public static Integer findInteger(String body, String... fieldNames) {
        if (body == null || body.isBlank()) return null;
        for (String fieldName : fieldNames) {
            Pattern pattern = Pattern.compile(
                    "(?i)[\"']?" + Pattern.quote(fieldName) + "[\"']?\\s*[:=]\\s*[\"']?(\\d{1,9})");
            Matcher matcher = pattern.matcher(body);
            if (matcher.find()) {
                try {
                    return Integer.valueOf(matcher.group(1));
                } catch (NumberFormatException ignored) {
                    // Fall through to the next candidate field.
                }
            }
        }
        return null;
    }
}
