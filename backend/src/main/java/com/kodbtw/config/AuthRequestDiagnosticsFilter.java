package com.kodbtw.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/** Adds safe request correlation and timing for authentication POSTs only. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuthRequestDiagnosticsFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final Logger log = LoggerFactory.getLogger(AuthRequestDiagnosticsFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = requestPath(request);
        return !"/api/auth/login".equals(path) && !"/api/auth/register".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = parseRequestId(request.getHeader(REQUEST_ID_HEADER));
        String operation = "/api/auth/login".equals(requestPath(request)) ? "login" : "register";
        String previousRequestId = MDC.get(REQUEST_ID_MDC_KEY);
        long startedAt = System.nanoTime();
        String exceptionCategory = "none";

        response.setHeader(REQUEST_ID_HEADER, requestId);
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        log.info("auth_request_started request_id={} operation={}", requestId, operation);
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException exception) {
            exceptionCategory = exception.getClass().getSimpleName();
            throw exception;
        } catch (RuntimeException exception) {
            exceptionCategory = exception.getClass().getSimpleName();
            throw exception;
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("auth_request_completed request_id={} operation={} status={} duration_ms={} exception_category={}",
                    requestId, operation, response.getStatus(), durationMs, exceptionCategory);
            if (previousRequestId == null) {
                MDC.remove(REQUEST_ID_MDC_KEY);
            } else {
                MDC.put(REQUEST_ID_MDC_KEY, previousRequestId);
            }
        }
    }

    private String parseRequestId(String suppliedRequestId) {
        if (suppliedRequestId != null) {
            try {
                return UUID.fromString(suppliedRequestId).toString();
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed client values and use a server-generated identifier.
            }
        }
        return UUID.randomUUID().toString();
    }

    private String requestPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        return contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)
                ? path.substring(contextPath.length())
                : path;
    }
}
