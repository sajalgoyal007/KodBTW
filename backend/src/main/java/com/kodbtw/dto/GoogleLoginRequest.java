package com.kodbtw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleLoginRequest(
        @NotBlank @Size(max = 8192) String credential,
        @NotBlank @Size(max = 36) String challengeId
) {}
