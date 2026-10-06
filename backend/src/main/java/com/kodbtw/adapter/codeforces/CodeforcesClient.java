package com.kodbtw.adapter.codeforces;

import com.kodbtw.adapter.codeforces.dto.CodeforcesDto;

/**
 * Client abstraction for communicating with the Codeforces public API.
 *
 * <p>Uses the official Codeforces API endpoint:
 * {@code https://codeforces.com/api/user.info?handles=<handle>}
 *
 * <p>This is a public, unauthenticated API. No credentials, cookies, or tokens are required.
 */
public interface CodeforcesClient {

    /**
     * Fetches public user information from Codeforces.
     *
     * @param handle the public Codeforces handle
     * @return the user information from the API
     * @throws com.kodbtw.exception.ResourceNotFoundException if the user does not exist on Codeforces
     * @throws com.kodbtw.exception.PlatformApiException      if an upstream API error, timeout, rate limit, or network failure occurs
     */
    CodeforcesDto.UserInfo fetchUserInfo(String handle);
}
