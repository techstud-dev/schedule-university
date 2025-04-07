package com.techstud.schedule_university.auth;

import com.techstud.schedule_university.auth.dto.request.RegisterDTO;
import com.techstud.schedule_university.auth.entity.PendingRegistration;
import com.techstud.schedule_university.auth.exception.InvalidCodeException;
import com.techstud.schedule_university.auth.repository.PendingRegistrationRepository;
import com.techstud.schedule_university.auth.service.CodeGeneratorService;
import com.techstud.schedule_university.auth.service.EmailService;
import com.techstud.schedule_university.auth.service.impl.EmailConfirmationServiceImpl;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailConfirmationServiceImplTest {

    @Mock
    private PendingRegistrationRepository repository;

    @Mock
    private CodeGeneratorService codeGeneratorService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmailConfirmationServiceImpl confirmationService;

    @Test
    void initiateConfirmation_ShouldSaveDataAndSendEmail() throws MessagingException {
        // Arrange
        RegisterDTO dto = new RegisterDTO(
                "user", "test@mail.com", "+123456789",
                "password", "User Name", "Group-1", "University"
        );
        String mockCode = "123456";
        String encodedPassword = "encoded_password";

        when(codeGeneratorService.generateCode()).thenReturn(mockCode);
        when(passwordEncoder.encode(dto.password())).thenReturn(encodedPassword);

        // Act
        String resultCode = confirmationService.initiateConfirmation(dto);

        // Assert
        ArgumentCaptor<PendingRegistration> captor = ArgumentCaptor.forClass(PendingRegistration.class);
        verify(repository).save(captor.capture());
        verify(emailService).sendCode(dto.email(), mockCode);

        PendingRegistration saved = captor.getValue();
        assertAll(
                () -> assertEquals(mockCode, resultCode),
                () -> assertEquals(dto.username(), saved.getUsername()),
                () -> assertEquals(encodedPassword, saved.getPassword()),
                () -> assertTrue(saved.getExpirationDate().isAfter(Instant.now()))
        );
    }

    @Test
    void validateCode_ValidCode_ReturnsPendingRegistration() throws Exception {
        // Arrange
        String code = "123456";
        PendingRegistration pending = PendingRegistration.builder()
                .expirationDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        when(repository.findByConfirmationCode(code)).thenReturn(Optional.of(pending));

        // Act
        PendingRegistration result = confirmationService.validateCode(code);

        // Assert
        assertEquals(pending, result);
    }

    @Test
    void validateCode_ExpiredCode_ThrowsException() {
        // Arrange
        String code = "123456";
        PendingRegistration pending = PendingRegistration.builder()
                .expirationDate(Instant.now().minus(1, ChronoUnit.MINUTES))
                .build();

        when(repository.findByConfirmationCode(code)).thenReturn(Optional.of(pending));

        // Act & Assert
        assertThrows(InvalidCodeException.class,
                () -> confirmationService.validateCode(code)
        );
    }
}
