package com.kodbtw.adapter.atcoder;

import com.kodbtw.adapter.atcoder.dto.AtCoderAcRankResponse;
import com.kodbtw.adapter.atcoder.dto.AtCoderHistoryEntry;
import com.kodbtw.adapter.atcoder.dto.AtCoderStats;
import com.kodbtw.adapter.atcoder.dto.AtCoderSubmission;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Client for AtCoder's public rating history plus Kenkoooo's public AtCoder API.
 *
 * <p>All endpoints are unauthenticated and read-only. No cookies, session state, or
 * browser automation is used.
 */
@Component
public class AtCoderClientImpl implements AtCoderClient {

    private static final String HISTORY_URL = "https://atcoder.jp/users/{handle}/history/json";
    private static final String AC_RANK_URL = "https://kenkoooo.com/atcoder/atcoder-api/v3/user/ac_rank";
    private static final String SUBMISSIONS_URL = "https://kenkoooo.com/atcoder/atcoder-api/v3/user/submissions";
    private static final String USER_AGENT = "KodBTW/1.0 (coding-profile statistics; contact via project repository)";
    private static final Duration SUBMISSION_WINDOW = Duration.ofDays(365);
    private static final String HANDLE_PATTERN = "[A-Za-z0-9_.-]{1,50}";

    private final RestClient restClient;

    public AtCoderClientImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public AtCoderClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public AtCoderStats fetchStats(String username) {
        if (username == null || !username.trim().matches(HANDLE_PATTERN)) {
            throw new ResourceNotFoundException("AtCoder handle is invalid");
        }
        String handle = username.trim();

        AtCoderHistoryEntry[] history = get(HISTORY_URL, AtCoderHistoryEntry[].class, handle);
        AtCoderAcRankResponse acRank = get(AC_RANK_URL + "?user={handle}", AtCoderAcRankResponse.class, handle);
        long fromSecond = Instant.now().minus(SUBMISSION_WINDOW).getEpochSecond();
        AtCoderSubmission[] submissions = get(
                SUBMISSIONS_URL + "?user={handle}&from_second={from}", AtCoderSubmission[].class, handle, fromSecond);

        Integer rating = null;
        Integer maxRating = null;
        Integer contests = history == null ? null : history.length;
        if (history != null) {
            for (AtCoderHistoryEntry entry : history) {
                if (entry == null || entry.newRating() == null) continue;
                rating = entry.newRating();
                if (maxRating == null || entry.newRating() > maxRating) maxRating = entry.newRating();
            }
        }

        Integer totalSolved = acRank == null ? null : acRank.count();
        Integer rank = acRank == null ? null : acRank.rank();
        int[] streaks = streakDays(submissions);

        Integer currentStreak = submissions == null ? null : streaks[0];
        Integer longestStreak = submissions == null ? null : streaks[1];

        if (rating == null && totalSolved == null && rank == null && currentStreak == null) {
            throw new PlatformApiException("AtCoder APIs returned no usable statistics");
        }

        return new AtCoderStats(rating, maxRating, rank, totalSolved, contests, currentStreak, longestStreak);
    }

    private <T> T get(String uri, Class<T> type, Object... variables) {
        try {
            return restClient.get()
                    .uri(uri, variables)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(type);
        } catch (HttpStatusCodeException exception) {
            int status = exception.getStatusCode().value();
            if (status == 404) {
                throw new ResourceNotFoundException("AtCoder profile not found");
            }
            if (status == 403) {
                throw new PlatformApiException("AtCoder access was denied", exception);
            }
            if (status == 429) {
                throw new PlatformApiException("AtCoder API rate limit exceeded", exception);
            }
            if (status >= 500) {
                throw new PlatformApiException("AtCoder API returned an HTTP error", exception);
            }
            throw new PlatformApiException("AtCoder API returned an unexpected HTTP status", exception);
        } catch (RestClientException exception) {
            throw new PlatformApiException("Failed to communicate with AtCoder API", exception);
        }
    }

    /** Returns {currentStreak, longestStreak} based on distinct UTC days with accepted submissions. */
    private int[] streakDays(AtCoderSubmission[] submissions) {
        if (submissions == null) return new int[]{0, 0};

        TreeSet<LocalDate> acceptedDays = new TreeSet<>();
        for (AtCoderSubmission submission : submissions) {
            if (submission == null || submission.epochSecond() == null || submission.result() == null) continue;
            if (!"AC".equalsIgnoreCase(submission.result())) continue;
            acceptedDays.add(LocalDate.ofInstant(
                    Instant.ofEpochSecond(submission.epochSecond()), ZoneOffset.UTC));
        }
        if (acceptedDays.isEmpty()) return new int[]{0, 0};

        List<LocalDate> days = new ArrayList<>(acceptedDays);
        int longest = 1;
        int running = 1;
        for (int i = 1; i < days.size(); i++) {
            if (days.get(i).minusDays(1).equals(days.get(i - 1))) {
                running++;
                longest = Math.max(longest, running);
            } else {
                running = 1;
            }
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate mostRecent = days.get(days.size() - 1);
        int current = 0;
        if (!mostRecent.isBefore(today.minusDays(1))) {
            current = 1;
            for (int i = days.size() - 1; i > 0; i--) {
                if (days.get(i).minusDays(1).equals(days.get(i - 1))) current++;
                else break;
            }
        }
        return new int[]{current, longest};
    }
}
