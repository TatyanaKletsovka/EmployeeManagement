package com.syberry.bakery.service.impl;

import com.syberry.bakery.dto.EmailDetails;
import com.syberry.bakery.exception.EmailException;
import com.syberry.bakery.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(EmailDetails emailDetails) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setTo(emailDetails.getRecipient());
            helper.setSubject(emailDetails.getSubject());
            helper.setText(emailDetails.getMsgBody(), true);
        } catch (MessagingException e) {
            throw new EmailException(e, "Failed to send an email");
        }
        mailSender.send(message);
    }

    @Override
    public String getTemplate(String htmlTemplate, Map<String, String> values) {
        for (String key : values.keySet()) {
            htmlTemplate = htmlTemplate.replace("{{" + key + "}}", values.get(key));
        }
        return htmlTemplate;
    }

    @Override
    public void sendEmailVerificationCode(String code, String userEmail) {
        String emailTemplate;
        try {
            final File jsonFile = new ClassPathResource("html/email-verification.html").getFile();
            emailTemplate = Files.readString(jsonFile.toPath());
        } catch (IOException e) {
            throw new EmailException("Failed to send verification email");
        }
        Map<String, String> values = new HashMap<>();
        values.put("code", code);
        EmailDetails email = new EmailDetails(userEmail,
                getTemplate(emailTemplate, values), "2 Factor Authentication");
        sendEmail(email);
    }

    @Override
    public void sendResetPasswordEmail(String token, String userEmail) {
        String emailTemplate;
        try {
            final File jsonFile = new ClassPathResource("html/reset-password-email.html").getFile();
            emailTemplate = Files.readString(jsonFile.toPath());
        } catch (IOException e) {
            throw new EmailException("Failed to send verification email");
        }
        Map<String, String> values = new HashMap<>();
        values.put("token", token);
        EmailDetails email = new EmailDetails(userEmail, getTemplate(emailTemplate, values), "Reset Password");
        sendEmail(email);
    }
}
