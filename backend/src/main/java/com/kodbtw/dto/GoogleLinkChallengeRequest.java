package com.kodbtw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleLinkChallengeRequest(
        @NotBlank @Size(max = 1024) String currentPassword
) {
    @Override
    public String toString() {
        return "GoogleLinkChallengeRequest[redacted]";
    }
}
