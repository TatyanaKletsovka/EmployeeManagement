package com.syberry.bakery.service;

import com.syberry.bakery.dto.EmailDetails;

import java.util.Map;

public interface EmailService {
    void sendEmail(EmailDetails emailDetails);

    String getTemplate(String htmlTemplate, Map<String, String> values);

    void sendEmailVerificationCode(String code, String userEmail);
    void sendResetPasswordEmail(String token, String userEmail);
}
