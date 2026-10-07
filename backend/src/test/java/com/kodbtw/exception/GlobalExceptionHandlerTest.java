package com.kodbtw.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void upstreamErrorResponseDoesNotExposeProviderExceptionDetails() {
        var response = handler.handlePlatformApi(new PlatformApiException("upstream-secret api-key=hidden"));

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("The platform is temporarily unavailable. Please try again later.", response.getBody().get("message"));
        assertFalse(response.getBody().toString().contains("upstream-secret"));
        assertFalse(response.getBody().toString().contains("api-key"));
    }

    @Test
    void unexpectedErrorResponseDoesNotExposeInternalDetails() {
        var response = handler.handleGenericException(new RuntimeException("SQL connection failed with password=hidden"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().get("message"));
        assertFalse(response.getBody().toString().contains("SQL connection"));
        assertFalse(response.getBody().toString().contains("password"));
    }
}
