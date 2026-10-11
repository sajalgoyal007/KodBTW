package com.kodbtw.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthRequestDiagnosticsFilterTest {

    @Test
    void correlatesAuthRequestAndLogsOnlySafeMetadata() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(AuthRequestDiagnosticsFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        String requestId = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setServletPath("/api/auth/login");
        request.addHeader(AuthRequestDiagnosticsFilter.REQUEST_ID_HEADER, requestId);
        request.addHeader("Authorization", "Bearer token-must-not-be-logged");
        request.setContent("{\"email\":\"private@example.com\",\"password\":\"password-must-not-be-logged\"}"
                .getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            new AuthRequestDiagnosticsFilter().doFilter(request, response,
                    (incomingRequest, outgoingResponse) -> ((MockHttpServletResponse) outgoingResponse).setStatus(200));

            assertTrue(response.getHeader(AuthRequestDiagnosticsFilter.REQUEST_ID_HEADER).equals(requestId));
            String logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage)
                    .reduce("", (left, right) -> left + "\n" + right);
            assertTrue(logs.contains("request_id=" + requestId));
            assertTrue(logs.contains("operation=login"));
            assertTrue(logs.contains("status=200"));
            assertTrue(logs.contains("duration_ms="));
            assertFalse(logs.contains("private@example.com"));
            assertFalse(logs.contains("password-must-not-be-logged"));
            assertFalse(logs.contains("token-must-not-be-logged"));
            assertTrue(appender.list.stream().allMatch(event -> event.getThrowableProxy() == null));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void malformedRequestIdIsReplacedAndExceptionDetailsAreNotLogged() throws ServletException, IOException {
        Logger logger = (Logger) LoggerFactory.getLogger(AuthRequestDiagnosticsFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");
        request.setServletPath("/api/auth/register");
        request.addHeader(AuthRequestDiagnosticsFilter.REQUEST_ID_HEADER, "not-a-uuid-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            try {
                new AuthRequestDiagnosticsFilter().doFilter(request, response, (incomingRequest, outgoingResponse) -> {
                    throw new ServletException("database-password=private");
                });
            } catch (ServletException expected) {
                assertTrue(expected.getMessage().contains("database-password"));
            }

            String responseId = (String) response.getHeader(AuthRequestDiagnosticsFilter.REQUEST_ID_HEADER);
            assertTrue(UUID.fromString(responseId) != null);
            String logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage)
                    .reduce("", (left, right) -> left + "\n" + right);
            assertFalse(logs.contains("not-a-uuid-secret"));
            assertFalse(logs.contains("database-password"));
            assertTrue(logs.contains("exception_category=ServletException"));
            assertTrue(appender.list.stream().allMatch(event -> event.getThrowableProxy() == null));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
