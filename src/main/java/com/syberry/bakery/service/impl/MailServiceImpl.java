package com.syberry.bakery.service.impl;

import com.syberry.bakery.dto.EmailDetails;
import com.syberry.bakery.exception.MailException;
import com.syberry.bakery.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(EmailDetails emailDetails) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setTo(emailDetails.getRecipient());
            helper.setSubject(emailDetails.getSubject());
            helper.setText(emailDetails.getMsgBody());
        } catch (MessagingException e) {
            throw new MailException(e, "Failed to send an email");
        }
        mailSender.send(message);
    }
}
