package com.kodbtw.contest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** Guard so the calendar can only ever link out to plain HTTP(S) contest pages. */
public final class ContestUrls {

    private ContestUrls() {
    }

    public static boolean isSafeHttpUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String trimmed = value.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return false;
        }
        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme();
            if (scheme == null
                    || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                return false;
            }
            return uri.getHost() != null && !uri.getHost().isBlank();
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
