package com.techstud.schedule_university.auth;

import com.techstud.schedule_university.auth.dto.request.RegistrationRecord;
import com.techstud.schedule_university.auth.entity.PendingRegistration;
import com.techstud.schedule_university.auth.exception.InvalidCodeException;
import com.techstud.schedule_university.auth.exception.ResendTooOftenException;
import com.techstud.schedule_university.auth.repository.PendingRegistrationRepository;
import com.techstud.schedule_university.auth.service.CodeGeneratorService;
import com.techstud.schedule_university.auth.service.impl.EmailConfirmationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.techstud.schedule_university.auth.util.ConstantsUtil.CLEANUP_BATCH_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailConfirmationServiceImplTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_CODE = "654321";

    @Mock
    private PendingRegistrationRepository repository;

    @Mock
    private CodeGeneratorService codeGeneratorService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EmailConfirmationServiceImpl confirmationService;

    @Test
    void initiateConfirmation_ShouldUpdateExistingRecord() {
        // Arrange
        RegistrationRecord dto = createTestRegistration();
        when(codeGeneratorService.generateCode()).thenReturn(TEST_CODE);
        when(repository.updateExisting(any(), any(), any(), any())).thenReturn(1);

        // Act
        String resultCode = confirmationService.initiateConfirmation(dto);

        // Assert
        verify(repository, never()).save(any());
        verify(eventPublisher).publishEvent(any(EmailConfirmationServiceImpl.ConfirmationEvent.class));
        assertEquals(TEST_CODE, resultCode);
    }

    @Test
    void initiateConfirmation_ShouldCreateNewRecord() {
        // Arrange
        RegistrationRecord dto = createTestRegistration();
        when(codeGeneratorService.generateCode()).thenReturn(TEST_CODE);
        when(repository.updateExisting(any(), any(), any(), any())).thenReturn(0);

        // Act
        confirmationService.initiateConfirmation(dto);

        // Assert
        ArgumentCaptor<PendingRegistration> captor = ArgumentCaptor.forClass(PendingRegistration.class);
        verify(repository).save(captor.capture());

        PendingRegistration saved = captor.getValue();
        assertAll(
                () -> assertEquals(dto.email(), saved.getEmail()),
                () -> assertEquals(TEST_CODE, saved.getConfirmationCode())
        );
    }

    @Test
    void validateCode_ShouldThrowWhenCodeExpired() {
        // Arrange
        PendingRegistration expired = PendingRegistration.builder()
                .expirationDate(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();
        when(repository.findByConfirmationCode(TEST_CODE)).thenReturn(Optional.of(expired));

        // Act & Assert
        assertThrows(InvalidCodeException.class,
                () -> confirmationService.validateCode(TEST_CODE));
    }

    @Test
    void resendConfirmationCode_ShouldThrowWhenTooSoon() {
        // Arrange
        PendingRegistration pending = PendingRegistration.builder()
                .lastSentTime(Instant.now().minus(1, ChronoUnit.MINUTES))
                .build();

        when(repository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(pending));

        // Act & Assert
        assertThrows(ResendTooOftenException.class,
                () -> confirmationService.resendConfirmationCode(TEST_EMAIL));
    }

    @Test
    void cleanupExpiredRegistrations_ShouldDeleteInBatches() {
        // Arrange
        when(repository.cleanupChunk(CLEANUP_BATCH_SIZE))
                .thenReturn(CLEANUP_BATCH_SIZE, CLEANUP_BATCH_SIZE, 500);

        // Act
        confirmationService.cleanupExpiredRegistrations();

        // Assert
        verify(repository, times(3)).cleanupChunk(CLEANUP_BATCH_SIZE);
    }

    private RegistrationRecord createTestRegistration() {
        return new RegistrationRecord(
                "testuser",
                TEST_EMAIL,
                "+123456789",
                "password",
                "Test User",
                "Group-1",
                "University"
        );
    }
}
