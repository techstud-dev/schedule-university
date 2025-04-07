package com.techstud.schedule_university.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.exception.InvalidJwtTokenException;
import com.techstud.schedule_university.auth.repository.UserRepository;
import com.techstud.schedule_university.auth.security.TokenService;
import com.techstud.schedule_university.auth.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Test
    void refreshToken_ShouldReturnNewToken_WhenValidTokenProvided() throws Exception {
        String validToken = "valid.token.here";
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        User user = new User();
        user.setUsername("testUser");

        when(tokenService.verifyToken(validToken)).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("testUser");
        when(userRepository.findByUsernameIgnoreCase("testUser")).thenReturn(Optional.of(user));
        when(tokenService.generateToken(user)).thenReturn("newAccessToken");

        String result = refreshTokenService.refreshToken(validToken);

        assertEquals("newAccessToken", result);
        verify(tokenService).verifyToken(validToken);
        verify(userRepository).findByUsernameIgnoreCase("testUser");
        verify(tokenService).generateToken(user);
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenIsNullOrEmpty() {
        String nullToken = null;
        String emptyToken = "";

        assertThrows(InvalidJwtTokenException.class, () -> refreshTokenService.refreshToken(nullToken));
        assertThrows(InvalidJwtTokenException.class, () -> refreshTokenService.refreshToken(emptyToken));
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenVerificationFails() throws InvalidJwtTokenException {
        String invalidToken = "invalid.token";
        when(tokenService.verifyToken(invalidToken)).thenReturn(null);

        assertThrows(InvalidJwtTokenException.class, () -> refreshTokenService.refreshToken(invalidToken));
    }

    @Test
    void refreshToken_ShouldThrowException_WhenUserNotFound() throws InvalidJwtTokenException {
        String validToken = "valid.token.here";
        DecodedJWT decodedJWT = mock(DecodedJWT.class);

        when(tokenService.verifyToken(validToken)).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("nonExistingUser");
        when(userRepository.findByUsernameIgnoreCase("nonExistingUser")).thenReturn(Optional.empty());

        assertThrows(InvalidJwtTokenException.class, () -> refreshTokenService.refreshToken(validToken));
    }
}
