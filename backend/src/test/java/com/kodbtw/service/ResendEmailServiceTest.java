package com.kodbtw.service;

import com.kodbtw.exception.EmailDeliveryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ResendEmailServiceTest {

    private static final String BASE_URL = "https://api.resend.com";
    private static final String API_KEY = "re_test_api_key";

    private MockRestServiceServer server;
    private RestClient.Builder restClientBuilder;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void sendsVerificationEmailWithBearerAuthAndEscapedHtml() {
        server.expect(requestTo(BASE_URL + "/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.from").value("KodBTW <onboarding@resend.dev>"))
                .andExpect(jsonPath("$.to[0]").value("recipient@example.com"))
                .andExpect(jsonPath("$.subject").value("Verify your KodBTW account"))
                .andExpect(jsonPath("$.html", containsString(
                        "&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;")))
                .andExpect(jsonPath("$.html", containsString("A&amp;B")))
                .andExpect(jsonPath("$.html", containsString(
                        "https://app.example/verify-email?token=raw-token")))
                .andRespond(withSuccess());

        service().sendVerificationEmail(
                " recipient@example.com ", "A&B <script>alert(\"x\")</script>",
                "https://app.example/verify-email?token=raw-token");

        server.verify();
    }

    @Test
    void usesFallbackDisplayNameWhenRecipientNameIsBlank() {
        server.expect(requestTo(BASE_URL + "/emails"))
                .andExpect(jsonPath("$.html", containsString("Hello Developer")))
                .andRespond(withSuccess());

        service().sendVerificationEmail("recipient@example.com", "   ", "https://app.example/verify-email?token=t");

        server.verify();
    }

    @Test
    void rejectsDeliveryWhenApiKeyIsNotConfigured() {
        ResendEmailService service = new ResendEmailService(
                "   ", "KodBTW <onboarding@resend.dev>", BASE_URL, restClientBuilder);

        assertThatThrownBy(() -> service.sendVerificationEmail(
                "recipient@example.com", "Recipient", "https://app.example/verify-email?token=t"))
                .isInstanceOf(EmailDeliveryException.class)
                .hasMessageContaining("not configured");

        server.verify();
    }

    @Test
    void wrapsClientErrorAsEmailDeliveryException() {
        server.expect(requestTo(BASE_URL + "/emails"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"invalid from address\"}"));

        assertThatThrownBy(() -> service().sendVerificationEmail(
                "recipient@example.com", "Recipient", "https://app.example/verify-email?token=t"))
                .isInstanceOf(EmailDeliveryException.class)
                .hasMessageContaining("Failed to send verification email");

        server.verify();
    }

    @Test
    void wrapsServerErrorAsEmailDeliveryException() {
        server.expect(requestTo(BASE_URL + "/emails"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> service().sendVerificationEmail(
                "recipient@example.com", "Recipient", "https://app.example/verify-email?token=t"))
                .isInstanceOf(EmailDeliveryException.class)
                .hasMessageContaining("Failed to send verification email");

        server.verify();
    }

    private ResendEmailService service() {
        return new ResendEmailService(
                API_KEY, "KodBTW <onboarding@resend.dev>", BASE_URL, restClientBuilder);
    }
}
