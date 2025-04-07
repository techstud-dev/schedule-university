package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.dto.request.RegistrationRecord;
import com.techstud.schedule_university.auth.entity.PendingRegistration;
import com.techstud.schedule_university.auth.exception.InvalidCodeException;
import com.techstud.schedule_university.auth.exception.PendingRegistrationNotFoundException;
import com.techstud.schedule_university.auth.exception.ResendTooOftenException;
import com.techstud.schedule_university.auth.repository.PendingRegistrationRepository;
import com.techstud.schedule_university.auth.service.CodeGeneratorService;
import com.techstud.schedule_university.auth.service.EmailConfirmationService;
import com.techstud.schedule_university.auth.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.techstud.schedule_university.auth.util.ConstantsUtil.*;

/**
 * Сервис подтверждения email
 *
 * <p>Управляет процессом подтверждения регистрации через email</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailConfirmationServiceImpl implements EmailConfirmationService {
    private final PendingRegistrationRepository repository;
    private final CodeGeneratorService codeGeneratorService;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Инициирует процесс подтверждения
     *
     * @param dto Данные регистрации
     * @return Сгенерированный код подтверждения
     */
    @Override
    @Transactional
    public String initiateConfirmation(RegistrationRecord dto) {
        String code = codeGeneratorService.generateCode();
        saveConfirmationData(dto, code);
        eventPublisher.publishEvent(new ConfirmationEvent(dto.email(), code));
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

    @Override
    @Transactional
    public void resendConfirmationCode(String email)
            throws PendingRegistrationNotFoundException, ResendTooOftenException {
        PendingRegistration pending = repository.findByEmail(email)
                .orElseThrow(() -> new PendingRegistrationNotFoundException(email));

        validateResendInterval(pending.getLastSentTime());

        String newCode = codeGeneratorService.generateCode();
        repository.updateFields(pending.getId(), newCode,
                Instant.now().plus(5, ChronoUnit.MINUTES), Instant.now());

        eventPublisher.publishEvent(new ConfirmationEvent(email, newCode));
    }

    @Scheduled(fixedRate = FIVE_MINUTES_IN_MS)
    @Transactional
    public void cleanupExpiredRegistrations() {
        StopWatch watch = new StopWatch();
        watch.start();
        int totalDeleted = 0;

        int deleted;
        do {
            deleted = repository.cleanupChunk(CLEANUP_BATCH_SIZE);
            totalDeleted += deleted;
        } while(deleted == CLEANUP_BATCH_SIZE);

        watch.stop();
        log.info("Cleaned {} records in {} ms", totalDeleted, watch.getTotalTimeMillis());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailEvent(ConfirmationEvent event) {
        try {
            emailService.sendCodeAsync(event.email(), event.code());
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", event.email(), e);
        }
    }

    private void validateResendInterval(Instant lastSent) throws ResendTooOftenException {
        Instant nextAvailable = lastSent.plus(RESEND_INTERVAL);
        if (Instant.now().isBefore(nextAvailable)) {
            Duration remaining = Duration.between(Instant.now(), nextAvailable);
            throw new ResendTooOftenException(
                    "Resend will be able in %d:%02d"
                            .formatted(remaining.toMinutes(), remaining.toSecondsPart())
            );
        }
    }

    /**
     * Вспомогательный метод для сохранения PendingRegistration
     *
     * @param dto входной дто регистрации
     * @param code код для сохранения в Бд
     */
    private void saveConfirmationData(RegistrationRecord dto, String code) {
        int updated = repository.updateExisting(
                dto.email(),
                code,
                Instant.now().plus(5, ChronoUnit.MINUTES),
                Instant.now()
        );

        if (updated == NO_ROWS_UPDATED) {
            PendingRegistration newPending = PendingRegistration.builder()
                    .username(dto.username())
                    .fullName(dto.fullName())
                    .email(dto.email())
                    .password(dto.password())
                    .phoneNumber(dto.phoneNumber())
                    .groupNumber(dto.groupNumber())
                    .university(dto.university())
                    .confirmationCode(code)
                    .expirationDate(Instant.now().plus(5, ChronoUnit.MINUTES))
                    .lastSentTime(Instant.now())
                    .build();

            repository.save(newPending);
        }
    }

    public record ConfirmationEvent(String email, String code) {}
}
