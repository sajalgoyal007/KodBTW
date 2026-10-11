package com.kodbtw.adapter.contest;

import com.kodbtw.dto.contest.ContestDto;

import java.time.Instant;
import java.util.List;

/** Provider boundary for public contest schedules, independent of user-stat adapters. */
public interface ContestProvider {
    String platform();

    String source();

    List<ContestDto> fetchUpcoming(Instant fetchedAt);
}
