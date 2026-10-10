package com.kodbtw.adapter.codechef;

import com.kodbtw.adapter.codechef.dto.CodeChefApiResponse;

public interface CodeChefClient {
    CodeChefApiResponse fetchProfile(String handle);
}
