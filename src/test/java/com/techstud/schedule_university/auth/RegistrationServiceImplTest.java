package com.techstud.schedule_university.auth;

import com.techstud.schedule_university.auth.config.JwtProperties;
import com.techstud.schedule_university.auth.dto.request.RegisterDTO;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationDTO;
import com.techstud.schedule_university.auth.entity.RefreshToken;
import com.techstud.schedule_university.auth.entity.Role;
import com.techstud.schedule_university.auth.entity.University;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.repository.RoleRepository;
import com.techstud.schedule_university.auth.repository.UniversityRepository;
import com.techstud.schedule_university.auth.repository.UserRepository;
import com.techstud.schedule_university.auth.security.TokenService;
import com.techstud.schedule_university.auth.service.UserFactory;
import com.techstud.schedule_university.auth.service.impl.RegistrationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserFactory userFactory;

    @Mock
    private TokenService tokenService;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterDTO registerDto = new RegisterDTO(
                "username",
                "Full Name",
                "Password123",
                "email@example.com",
                "1234567890",
                "Group-1",
                "Some University"
        );

        University university = new University("Some University");
        university.setId(1L);

        Role userRole = new Role("USER");
        userRole.setId(1L);

        User user = User.builder()
                .username(registerDto.username())
                .fullName(registerDto.fullName())
                .password("EncryptedPassword123")
                .email(registerDto.email())
                .phoneNumber(registerDto.phoneNumber())
                .groupNumber(registerDto.groupNumber())
                .university(university)
                .roles(Set.of(userRole))
                .build();

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(7200L);

        when(universityRepository.findByName(registerDto.university()))
                .thenReturn(Optional.of(university));
        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.of(userRole));
        when(userFactory.createUser(
                registerDto.username(),
                registerDto.fullName(),
                registerDto.password(),
                registerDto.email(),
                registerDto.phoneNumber(),
                registerDto.groupNumber()
        )).thenReturn(user);
        when(tokenService.generateToken(user))
                .thenReturn(accessToken);
        when(tokenService.generateRefreshToken(user))
                .thenReturn(refreshToken);

        SuccessAuthenticationDTO result = registrationService.processRegister(registerDto);

        verify(userRepository).save(user);
        assertEquals(accessToken, result.token());
        assertEquals(refreshToken, result.refreshToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        RefreshToken savedRefreshToken = userCaptor.getValue().getRefreshToken();
        assertNotNull(savedRefreshToken);
        assertEquals(refreshToken, savedRefreshToken.getRefreshToken());

        assertTrue(savedRefreshToken.getExpiryDate().isAfter(Instant.now().plus(7190, ChronoUnit.SECONDS)));
        assertTrue(savedRefreshToken.getExpiryDate().isBefore(Instant.now().plus(7210, ChronoUnit.SECONDS)));

        verify(jwtProperties).getRefreshTokenExpiration();
    }
}
