package com.kodbtw.adapter.interviewbit;

import com.kodbtw.adapter.interviewbit.dto.InterviewBitStats;

public interface InterviewBitClient {

    InterviewBitStats fetchStats(String username);
}
