package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.dto.request.RegistrationRecord;
import com.techstud.schedule_university.auth.entity.PendingRegistration;
import com.techstud.schedule_university.auth.exception.InvalidCodeException;
import com.techstud.schedule_university.auth.repository.PendingRegistrationRepository;
import com.techstud.schedule_university.auth.service.CodeGeneratorService;
import com.techstud.schedule_university.auth.service.EmailConfirmationService;
import com.techstud.schedule_university.auth.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Сервис подтверждения email
 *
 * <p>Управляет процессом подтверждения регистрации через email</p>
 */
@Service
@RequiredArgsConstructor
public class EmailConfirmationServiceImpl implements EmailConfirmationService {
    private final PendingRegistrationRepository repository;
    private final CodeGeneratorService codeGeneratorService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Инициирует процесс подтверждения
     *
     * @param dto Данные регистрации
     * @return Сгенерированный код подтверждения
     * @throws MessagingException При ошибке отправки email
     */
    @Override
    @Transactional
    public String initiateConfirmation(RegistrationRecord dto) throws MessagingException {
        String code = codeGeneratorService.generateCode();
        saveConfirmationData(dto, code);
        emailService.sendCode(dto.email(), code);
        return code;
    }

    /**
     * Проверяет код подтверждения
     *
     * @param code 6-значный код
     * @return Данные ожидающей регистрации
     * @throws InvalidCodeException Если код неверен или истек
     */
    @Override
    @Transactional(readOnly = true)
    public PendingRegistration validateCode(String code) throws InvalidCodeException {
        return repository.findByConfirmationCode(code)
                .filter(p -> Instant.now().isBefore(p.getExpirationDate()))
                .orElseThrow(InvalidCodeException::new);
    }

    // Удаляет все записи раз в 5 минут, у которых истёк код действия
    @Scheduled(fixedRate = 15 * 60 * 1000)
    @Transactional
    protected void cleanupExpiredRegistrations() {
        repository.deleteByExpirationDateBefore(Instant.now());
    }

    /**
     * Вспомогательный метод для сохранения PendingRegistration
     *
     * @param dto входной дто регистрации
     * @param code код для сохранения в Бд
     */
    private void saveConfirmationData(RegistrationRecord dto, String code) {
        repository.findByEmail(dto.email()).ifPresent(repository::delete);

        PendingRegistration pendingRegistration = PendingRegistration.builder()
                .username(dto.username())
                .fullName(dto.fullName())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .phoneNumber(dto.phoneNumber())
                .groupNumber(dto.groupNumber())
                .university(dto.university())
                .confirmationCode(code)
                .expirationDate(Instant.now().plus(5, ChronoUnit.MINUTES))
                .build();

        repository.save(pendingRegistration);
    }
}
