package com.kodbtw.adapter.contest.feed;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/** Shared HTTP defaults for per-platform contest feeds. */
final class FeedHttp {

    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    static final Duration READ_TIMEOUT = Duration.ofSeconds(15);

    private FeedHttp() {
    }

    static RestClient client() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        return RestClient.builder().requestFactory(factory).build();
    }
}
