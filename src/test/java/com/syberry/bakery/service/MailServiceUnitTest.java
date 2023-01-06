package com.syberry.bakery.service;

import com.syberry.bakery.dto.EmailDetails;
import com.syberry.bakery.service.impl.MailServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MailServiceUnitTest {
    @InjectMocks
    private MailServiceImpl mailService;
    @Mock
    private JavaMailSender mailSender;

    @Test
    public void should_SuccessfullySendMail() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getDefaultInstance(new Properties())));
        mailService.sendEmail(new EmailDetails("test@mail.com", "test", "test"));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
