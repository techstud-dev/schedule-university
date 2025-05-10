package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;

/**
 * Сервис отправки электронной почты
 *
 * <p>Отправляет коды подтверждения по email асинхронно</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.sender}")
    private String sender;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Отправляет html форму с кодом подтверждения
     *
     * @param email Адрес получателя
     * @param code 6-значный код подтверждения
     * @throws MessagingException При ошибке отправки
     */
    @Async
    @Override
    public void sendCodeAsync(String email, String code) throws MessagingException {
        var context = new Context();
        context.setVariable("confirmationCode", code);

        var htmlContent = templateEngine.process("email-registration-notification", context);

        var message = mailSender.createMimeMessage();

        var helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(sender);
        helper.setTo(email);
        helper.setSubject("Подтверждение регистрации");
        helper.setText(htmlContent, true);

        var res = new FileSystemResource(new File("src/main/resources/static/tech-stud-university-background.png"));
        helper.addInline("headerImage", res);

        mailSender.send(message);
        log.debug("Successfully sent email to {}", email);
    }
}
