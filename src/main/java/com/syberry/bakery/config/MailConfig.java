package com.syberry.bakery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    @Value("${bakery.mail.host}")
    private String host;
    @Value("${bakery.mail.port}")
    private int port;
    @Value("${bakery.mail.username}")
    private String username;
    @Value("${bakery.mail.password}")
    private String password;
    @Value("${bakery.mail.protocol}")
    private String protocol;
    @Value("${bakery.mail.auth}")
    private String auth;
    @Value("${bakery.mail.starttls}")
    private String starttls;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttls);

        return mailSender;
    }
}
