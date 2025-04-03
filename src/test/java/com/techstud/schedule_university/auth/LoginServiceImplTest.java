package com.techstud.schedule_university.auth;

import com.techstud.schedule_university.auth.config.JwtProperties;
import com.techstud.schedule_university.auth.dto.request.LoginDTO;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationDTO;
import com.techstud.schedule_university.auth.entity.RefreshToken;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.repository.UserRepository;
import com.techstud.schedule_university.auth.security.TokenService;
import com.techstud.schedule_university.auth.service.impl.LoginServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private LoginServiceImpl loginService;

    @Test
    void processLogin_ShouldReturnSuccessResponse_WhenCredentialsAreValid() throws Exception {
        LoginDTO loginDto = new LoginDTO("username", "password");
        User user = new User();
        user.setPassword("hashedPassword");

        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(7200L);

        when(userRepository.findByUsernameIgnoreCase("username"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword"))
                .thenReturn(true);
        when(tokenService.generateToken(eq(user)))
                .thenReturn("access-token");
        when(tokenService.generateRefreshToken(eq(user)))
                .thenReturn("new-refresh-token");

        SuccessAuthenticationDTO response = loginService.processLogin(loginDto);

        assertNotNull(response);
        assertEquals("access-token", response.token());
        assertEquals("new-refresh-token", response.refreshToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        RefreshToken savedRefreshToken = userCaptor.getValue().getRefreshToken();
        assertNotNull(savedRefreshToken);
        assertEquals("new-refresh-token", savedRefreshToken.getRefreshToken());

        assertTrue(savedRefreshToken.getExpiryDate().isAfter(Instant.now().plus(7190, ChronoUnit.SECONDS)));
        assertTrue(savedRefreshToken.getExpiryDate().isBefore(Instant.now().plus(7210, ChronoUnit.SECONDS)));

        verify(jwtProperties).getRefreshTokenExpiration();
    }
}
