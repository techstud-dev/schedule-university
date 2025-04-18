package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Сервис отправки электронной почты
 *
 * <p>Отправляет коды подтверждения по email асинхронно</p>
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.sender}")
    private String sender;

    private final JavaMailSender mailSender;

    /**
     * Отправляет код подтверждения
     *
     * @param email Адрес получателя
     * @param code 6-значный код подтверждения
     * @throws MessagingException При ошибке отправки
     */
    @Async
    @Override
    public void sendCodeAsync(String email, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(sender);
        helper.setTo(email);
        helper.setSubject("Your Verification Code");
        helper.setText("Confirmation code: " + code);

        mailSender.send(message);
    }
}
