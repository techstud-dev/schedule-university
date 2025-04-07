package com.techstud.schedule_university.auth;

import com.techstud.schedule_university.auth.exception.UserNotFoundException;
import com.techstud.schedule_university.auth.repository.UserRepository;
import com.techstud.schedule_university.auth.service.impl.LogoutServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.techstud.schedule_university.auth.UtilConstants.*;
import static com.techstud.schedule_university.auth.util.UtilConstants.NO_UPDATED_RECORDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogoutServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LogoutServiceImpl logoutService;

    @Test
    void logout_WithValidToken_ShouldClearRefreshToken() throws Exception {
        // Arrange
        when(userRepository.clearRefreshToken(anyString())).thenReturn(UPDATED_RECORDS);

        // Act
        logoutService.logout(VALID_TOKEN);

        // Assert
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).clearRefreshToken(tokenCaptor.capture());

        assertEquals(VALID_TOKEN, tokenCaptor.getValue());
        verify(userRepository, times(1)).clearRefreshToken(anyString());
    }

    @Test
    void logout_WithInvalidToken_ShouldThrowException() {
        // Arrange
        when(userRepository.clearRefreshToken(anyString())).thenReturn(NO_UPDATED_RECORDS);

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> logoutService.logout(INVALID_TOKEN)
        );

        assertEquals("No user found with refresh token", exception.getMessage());
        verify(userRepository, times(1)).clearRefreshToken(anyString());
    }

    // STRESS TEST
    @Disabled
    @RepeatedTest(100)
    void logout_ShouldHandleMultipleRequests() {
        when(userRepository.clearRefreshToken(anyString())).thenReturn(UPDATED_RECORDS);
        assertDoesNotThrow(() -> logoutService.logout(VALID_TOKEN));
    }
}
