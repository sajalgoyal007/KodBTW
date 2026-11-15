package com.kodbtw.support;

import com.kodbtw.exception.EmailDeliveryException;
import com.kodbtw.service.EmailService;

import java.util.ArrayList;
import java.util.List;

/**
 * Test-only {@link EmailService} that records outgoing verification emails in memory
 * instead of making real HTTP calls. Delivery failures can be simulated per message.
 */
public class RecordingEmailService implements EmailService {

    public record SentEmail(String to, String name, String verificationUrl) {
    }

    private final List<SentEmail> sent = new ArrayList<>();
    private RuntimeException nextFailure;

    @Override
    public synchronized void sendVerificationEmail(String recipientEmail, String recipientName, String verificationUrl) {
        if (nextFailure != null) {
            RuntimeException failure = nextFailure;
            nextFailure = null;
            throw failure;
        }
        sent.add(new SentEmail(recipientEmail, recipientName, verificationUrl));
    }

    public synchronized List<SentEmail> sent() {
        return List.copyOf(sent);
    }

    public synchronized SentEmail lastSent() {
        return sent.isEmpty() ? null : sent.get(sent.size() - 1);
    }

    public synchronized void clear() {
        sent.clear();
        nextFailure = null;
    }

    public synchronized void failNextDelivery() {
        failNextDelivery(new EmailDeliveryException("Simulated email delivery failure"));
    }

    public synchronized void failNextDelivery(RuntimeException failure) {
        this.nextFailure = failure;
    }
}
