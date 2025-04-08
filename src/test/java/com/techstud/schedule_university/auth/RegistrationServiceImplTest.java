package com.techstud.schedule_university.auth;

import com.techstud.schedule_university.auth.config.TokenProperties;
import com.techstud.schedule_university.auth.dto.request.RegistrationRecord;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationRecord;
import com.techstud.schedule_university.auth.entity.PendingRegistration;
import com.techstud.schedule_university.auth.entity.RefreshToken;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.exception.UserExistsException;
import com.techstud.schedule_university.auth.repository.UserRepository;
import com.techstud.schedule_university.auth.security.TokenService;
import com.techstud.schedule_university.auth.service.EmailConfirmationService;
import com.techstud.schedule_university.auth.service.UserCreationService;
import com.techstud.schedule_university.auth.service.impl.RegistrationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceImplTest {

    @Mock
    private TokenProperties properties;

    @Mock
    private EmailConfirmationService emailConfirmationService;

    @Mock
    private UserCreationService userCreationService;

    @Mock
    private UserRepository repository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private RegistrationServiceImpl service;

    @Test
    void startRegistration_WhenUserExists_ThrowsException() {
        // Arrange
        RegistrationRecord dto = new RegistrationRecord(
                "testUser", "test@mail.com", "+123456789",
                "password", "Test User", "Group-1", "University"
        );

        when(repository.existsByUniqueFields(
                dto.username(),
                dto.email(),
                dto.phoneNumber()
        )).thenReturn(true);

        // Act & Assert
        assertThrows(UserExistsException.class,
                () -> service.startRegistration(dto)
        );
    }

    @Test
    void startRegistration_WhenNewUser_CallsConfirmationService() throws Exception {
        // Arrange
        RegistrationRecord dto = createValidRegisterDTO();
        when(repository.existsByUniqueFields(any(), any(), any())).thenReturn(false);

        // Act
        service.startRegistration(dto);

        // Assert
        verify(emailConfirmationService).initiateConfirmation(dto);
    }

    @Test
    void completeRegistration_ValidCode_ReturnsTokens() throws Exception {
        // Arrange
        String code = "123456";
        PendingRegistration pending = createPendingRegistration();
        User mockUser = new User();
        SuccessAuthenticationRecord tokens = new SuccessAuthenticationRecord("access", "refresh");

        when(emailConfirmationService.validateCode(code)).thenReturn(pending);
        when(userCreationService.createPendingUser(pending)).thenReturn(mockUser);
        when(tokenService.generateTokens(mockUser)).thenReturn(tokens);
        when(properties.getRefreshTokenExpiration()).thenReturn(3600L);

        // Act
        SuccessAuthenticationRecord result = service.completeRegistration(code);

        // Assert
        assertAll(
                () -> assertEquals(tokens.token(), result.token()),
                () -> assertEquals(tokens.refreshToken(), result.refreshToken())
        );

        verify(tokenService).generateTokens(mockUser);
    }

    @Test
    void completeRegistration_SavesUserWithRefreshToken() throws Exception {
        // Arrange
        String code = "123456";
        String refreshToken = "refresh_token";

        PendingRegistration pending = createPendingRegistration();
        User mockUser = new User();
        SuccessAuthenticationRecord tokens = new SuccessAuthenticationRecord("access", refreshToken);

        when(emailConfirmationService.validateCode(code)).thenReturn(pending);
        when(userCreationService.createPendingUser(pending)).thenReturn(mockUser);
        when(tokenService.generateTokens(mockUser)).thenReturn(tokens);
        when(properties.getRefreshTokenExpiration()).thenReturn(3600L);

        // Act
        service.completeRegistration(code);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());

        RefreshToken savedToken = userCaptor.getValue().getRefreshToken();
        assertAll(
                () -> assertEquals(refreshToken, savedToken.getRefreshToken()),
                () -> assertTrue(savedToken.getExpiryDate().isAfter(Instant.now())),
                () -> assertTrue(
                        ChronoUnit.SECONDS.between(Instant.now(), savedToken.getExpiryDate()) <= 3600
                )
        );
    }

    private RegistrationRecord createValidRegisterDTO() {
        return new RegistrationRecord(
                "testUser",
                "test@mail.com",
                "+123456789",
                "password",
                "Test User",
                "Group-1",
                "University"
        );
    }

    private PendingRegistration createPendingRegistration() {
        return PendingRegistration.builder()
                .username("testUser")
                .email("test@mail.com")
                .phoneNumber("+123456789")
                .password("password")
                .fullName("Test User")
                .groupNumber("Group-1")
                .university("University")
                .build();
    }
}
