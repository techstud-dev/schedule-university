package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.service.CodeGeneratorService;
import com.techstud.schedule_university.auth.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.sender}")
    private String sender;

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendCode(String email, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(sender);
        helper.setTo(email);
        helper.setSubject("Schedule University Code");
        helper.setText("Email confirmation code " + code);

        mailSender.send(message);
    }
}
