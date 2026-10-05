package com.kodbtw.adapter.leetcode;

import com.kodbtw.adapter.leetcode.dto.LeetCodeDto.GraphQLData;

public interface LeetCodeClient {

    /**
     * Fetches public user profile statistics from LeetCode.
     *
     * @param username the public LeetCode username
     * @return the GraphQL data payload containing user profile and contest metrics
     * @throws com.kodbtw.exception.ResourceNotFoundException if the user does not exist on LeetCode
     * @throws com.kodbtw.exception.PlatformApiException      if an upstream API error, timeout, or network failure occurs
     */
    GraphQLData fetchUserProfile(String username);
}
