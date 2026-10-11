package com.kodbtw.adapter.contest.codeforces;

import java.util.List;

public interface CodeforcesContestClient {
    List<CodeforcesContestApiResponse.CodeforcesContestRecord> fetchContests();
}
