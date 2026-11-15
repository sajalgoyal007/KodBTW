package com.kodbtw.service;

public interface EmailService {

    void sendVerificationEmail(String recipientEmail, String recipientName, String verificationUrl);
}
