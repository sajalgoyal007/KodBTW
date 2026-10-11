package com.kodbtw.exception;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        boolean previousAdditivity = logger.isAdditive();
        logger.setAdditive(false);
        logger.addAppender(appender);
        var exception = new RuntimeException("SQL connection failed with password=hidden");
        try {
            var response = handler.handleGenericException(exception);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals("An unexpected error occurred. Please try again later.", response.getBody().get("message"));
            assertFalse(response.getBody().toString().contains("SQL connection"));
            assertFalse(response.getBody().toString().contains("password"));
            assertEquals(1, appender.list.size());
            assertTrue(appender.list.get(0).getFormattedMessage().contains(RuntimeException.class.getName()));
            assertFalse(appender.list.get(0).getFormattedMessage().contains("SQL connection"));
            assertFalse(appender.list.get(0).getFormattedMessage().contains("password"));
            assertNull(appender.list.get(0).getThrowableProxy());
        } finally {
            logger.detachAppender(appender);
            logger.setAdditive(previousAdditivity);
            appender.stop();
        }
    }
}
