package com.kodbtw.service;

import com.kodbtw.exception.EmailDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class ResendEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String fromEmail;

    public ResendEmailService(
            @Value("${app.resend.api-key:}") String apiKey,
            @Value("${app.resend.from-email:KodBTW <onboarding@resend.dev>}") String fromEmail,
            @Value("${app.resend.base-url:https://api.resend.com}") String baseUrl,
            RestClient.Builder restClientBuilder) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.fromEmail = fromEmail == null ? "KodBTW <onboarding@resend.dev>" : fromEmail.trim();
        this.restClient = restClientBuilder
                .baseUrl(baseUrl == null || baseUrl.isBlank() ? "https://api.resend.com" : baseUrl.trim())
                .build();
    }

    @Override
    public void sendVerificationEmail(String recipientEmail, String recipientName, String verificationUrl) {
        if (apiKey.isBlank()) {
            log.warn("Resend API key is not configured. Outgoing verification email suppressed.");
            throw new EmailDeliveryException("Resend API key is not configured on the server.");
        }

        Map<String, Object> payload = Map.of(
                "from", fromEmail,
                "to", List.of(recipientEmail.trim()),
                "subject", "Verify your KodBTW account",
                "html", buildHtmlEmail(recipientName, verificationUrl)
        );

        try {
            restClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Verification email successfully dispatched to domain {}", extractDomain(recipientEmail));
        } catch (RestClientException ex) {
            log.error("Failed to send verification email via Resend API: {}", ex.getMessage());
            throw new EmailDeliveryException("Failed to send verification email via Resend API.", ex);
        }
    }

    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) return "unknown";
        return email.substring(email.indexOf("@") + 1);
    }

    private String buildHtmlEmail(String name, String verificationUrl) {
        String displayName = name != null && !name.isBlank() ? name.trim() : "Developer";
        return "<div style=\"font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; max-width: 560px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 8px;\">"
                + "<h2 style=\"color: #0f172a; margin-top: 0;\">Welcome to KodBTW</h2>"
                + "<p style=\"color: #334155; font-size: 16px; line-height: 1.5;\">Hello " + escapeHtml(displayName) + ",</p>"
                + "<p style=\"color: #334155; font-size: 16px; line-height: 1.5;\">Thank you for registering. Please confirm your email address by clicking the button below:</p>"
                + "<div style=\"margin: 28px 0;\">"
                + "<a href=\"" + escapeHtml(verificationUrl) + "\" style=\"background-color: #6366f1; color: #ffffff; padding: 12px 24px; border-radius: 6px; text-decoration: none; font-weight: 600; display: inline-block;\">Verify Email Address</a>"
                + "</div>"
                + "<p style=\"color: #64748b; font-size: 14px; line-height: 1.5;\">This link is valid for 24 hours and can only be used once. If you did not create a KodBTW account, please disregard this message.</p>"
                + "</div>";
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
